package com.example.agrotech_api.service;

import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.exception.ResourceNotFoundException;
import com.example.agrotech_api.exception.UnauthorizedException;
import com.example.agrotech_api.model.RoleUsuario;
import com.example.agrotech_api.model.Funcionario;
import com.example.agrotech_api.model.Usuario;
import com.example.agrotech_api.model.dto.LoginRequestDTO;
import com.example.agrotech_api.model.dto.LoginResponseDTO;
import com.example.agrotech_api.model.dto.UsuarioRequestDTO;
import com.example.agrotech_api.model.dto.UsuarioResponseDTO;
import com.example.agrotech_api.model.dto.UsuarioUpdateDTO;
import com.example.agrotech_api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final FuncionarioService funcionarioService;
    private final PasswordEncoder passwordEncoder;

    // Registro thread-safe das sessões autenticadas no servidor (Token -> Usuario)
    private static final long DURACAO_SESSAO_HORAS = 8;

    private record Sessao(Usuario usuario, Instant expiraEm) {}

    private final Map<String, Sessao> sessoesAtivas = new ConcurrentHashMap<>();

    public UsuarioService(UsuarioRepository usuarioRepository,
                          FuncionarioService funcionarioService,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.funcionarioService = funcionarioService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDTO autenticar(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByUsernameComFuncionario(dto.username().trim())
                .orElseThrow(() -> new UnauthorizedException("Usuário ou senha inválidos."));

        if (!passwordEncoder.matches(dto.senha(), usuario.getSenhaHash())) {
            throw new UnauthorizedException("Usuário ou senha inválidos.");
        }

        String rawToken = usuario.getUsername() + ":" + usuario.getRole().name() + ":" + UUID.randomUUID();
        String token = Base64.getEncoder().encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));

        // Registra o token válido emitido pelo servidor
        Instant agora = Instant.now();
        sessoesAtivas.values().removeIf(s -> s.expiraEm().isBefore(agora));
        sessoesAtivas.put(token, new Sessao(usuario, agora.plusSeconds(DURACAO_SESSAO_HORAS * 3600)));

        return new LoginResponseDTO(
                token,
                usuario.getUsername(),
                usuario.getRole().name(),
                usuario.getFuncionario().getId(),
                usuario.getFuncionario().getNome()
        );
    }

    public Usuario validarToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Token de autenticação ausente.");
        }

        Sessao sessao = sessoesAtivas.get(token);
        if (sessao == null || sessao.expiraEm().isBefore(Instant.now())) {
            sessoesAtivas.remove(token);
            throw new UnauthorizedException("Token inválido ou sessão expirada.");
        }

        return sessao.usuario();
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        String username = dto.username().trim();
        if (usuarioRepository.existsByUsername(username)) {
            throw new BusinessRuleException("Já existe um usuário com o login: " + username);
        }
        if (usuarioRepository.existsByFuncionarioId(dto.funcionarioId())) {
            throw new BusinessRuleException("Este funcionário já possui um usuário de acesso cadastrado.");
        }

        Funcionario funcionario = funcionarioService.buscarPorId(dto.funcionarioId());

        Usuario usuario = Usuario.builder()
                .username(username)
                .senhaHash(passwordEncoder.encode(dto.senha()))
                .role(dto.role())
                .funcionario(funcionario)
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        return toDTO(salvo);
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findByIdComFuncionario(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));

        String username = dto.username().trim();
        if (!username.equals(usuario.getUsername()) && usuarioRepository.existsByUsername(username)) {
            throw new BusinessRuleException("Já existe um usuário com o login: " + username);
        }

        // Não permite deixar o sistema sem nenhum administrador
        if (usuario.getRole() == RoleUsuario.ADMIN && dto.role() != RoleUsuario.ADMIN
                && usuarioRepository.countByRole(RoleUsuario.ADMIN) <= 1) {
            throw new BusinessRuleException("Não é possível remover o perfil ADMIN do único administrador do sistema.");
        }

        boolean trocouSenha = dto.senha() != null && !dto.senha().isBlank();
        boolean alterouAcesso = trocouSenha
                || !username.equals(usuario.getUsername())
                || dto.role() != usuario.getRole();

        usuario.setUsername(username);
        usuario.setRole(dto.role());
        if (trocouSenha) {
            usuario.setSenhaHash(passwordEncoder.encode(dto.senha()));
        }
        Usuario salvo = usuarioRepository.save(usuario);

        // Sessões abertas guardam o perfil antigo: força novo login quando o acesso muda
        if (alterouAcesso) {
            sessoesAtivas.values().removeIf(s -> s.usuario().getId().equals(id));
        }
        return toDTO(salvo);
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAllComFuncionario().stream()
                .map(this::toDTO)
                .toList();
    }

    private UsuarioResponseDTO toDTO(Usuario u) {
        return new UsuarioResponseDTO(
                u.getId(),
                u.getUsername(),
                u.getRole(),
                u.getFuncionario().getId(),
                u.getFuncionario().getNome(),
                u.getFuncionario().getMatricula()
        );
    }
}