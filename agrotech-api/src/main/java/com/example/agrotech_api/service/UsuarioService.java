package com.example.agrotech_api.service;

import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.exception.UnauthorizedException;
import com.example.agrotech_api.model.Funcionario;
import com.example.agrotech_api.model.Usuario;
import com.example.agrotech_api.model.dto.LoginRequestDTO;
import com.example.agrotech_api.model.dto.LoginResponseDTO;
import com.example.agrotech_api.model.dto.UsuarioRequestDTO;
import com.example.agrotech_api.model.dto.UsuarioResponseDTO;
import com.example.agrotech_api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final Map<String, Usuario> sessoesAtivas = new ConcurrentHashMap<>();

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
        sessoesAtivas.put(token, usuario);

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

        Usuario usuario = sessoesAtivas.get(token);
        if (usuario == null) {
            throw new UnauthorizedException("Token inválido ou sessão expirada.");
        }

        return usuario;
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