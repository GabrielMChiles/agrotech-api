package com.example.agrotech_api.config;

import com.example.agrotech_api.model.*;
import com.example.agrotech_api.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final FornecedorRepository fornecedorRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(FornecedorRepository fornecedorRepository,
                          FuncionarioRepository funcionarioRepository,
                          UsuarioRepository usuarioRepository,
                          ProdutoRepository produtoRepository,
                          ClienteRepository clienteRepository,
                          PasswordEncoder passwordEncoder) {
        this.fornecedorRepository = fornecedorRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Fornecedor f1 = fornecedorRepository.findByCnpj("11.222.333/0001-44")
                .orElseGet(() -> fornecedorRepository.save(Fornecedor.builder()
                        .razaoSocial("AgroSementes Brasil")
                        .cnpj("11.222.333/0001-44")
                        .telefoneContato("(62) 3333-1010")
                        .build()));

        Fornecedor f2 = fornecedorRepository.findByCnpj("22.333.444/0001-55")
                .orElseGet(() -> fornecedorRepository.save(Fornecedor.builder()
                        .razaoSocial("Fertilizantes Cerrado")
                        .cnpj("22.333.444/0001-55")
                        .telefoneContato("(62) 3333-2020")
                        .build()));

        Funcionario func1 = funcionarioRepository.findByMatricula("M01")
                .orElseGet(() -> funcionarioRepository.save(Funcionario.builder()
                        .nome("Carlos Silva")
                        .matricula("M01")
                        .cargo("Gerente Comercial")
                        .build()));

        Funcionario func2 = funcionarioRepository.findByMatricula("M02")
                .orElseGet(() -> funcionarioRepository.save(Funcionario.builder()
                        .nome("Ana Souza")
                        .matricula("M02")
                        .cargo("Consultora de Vendas")
                        .build()));

        funcionarioRepository.findByMatricula("M03")
                .orElseGet(() -> funcionarioRepository.save(Funcionario.builder()
                        .nome("Marcos Oliveira")
                        .matricula("M03")
                        .cargo("Operador de Caixa")
                        .build()));

        if (!usuarioRepository.existsByUsername("admin")) {
            usuarioRepository.save(Usuario.builder()
                    .username("admin")
                    .senhaHash(passwordEncoder.encode("admin123"))
                    .role(RoleUsuario.ADMIN)
                    .funcionario(func1)
                    .build());
        }

        if (!usuarioRepository.existsByUsername("ana")) {
            usuarioRepository.save(Usuario.builder()
                    .username("ana")
                    .senhaHash(passwordEncoder.encode("ana123"))
                    .role(RoleUsuario.FUNCIONARIO)
                    .funcionario(func2)
                    .build());
        }

        if (produtoRepository.count() == 0) {
            produtoRepository.save(Produto.builder()
                    .nome("Semente de Soja Intacta 40kg")
                    .categoria("Sementes")
                    .precoAtual(new BigDecimal("420.00"))
                    .quantidadeEstoque(50)
                    .dataValidade(LocalDate.now().plusMonths(12))
                    .fornecedor(f1)
                    .build());

            produtoRepository.save(Produto.builder()
                    .nome("Adubo NPK 04-14-08 50kg")
                    .categoria("Fertilizantes")
                    .precoAtual(new BigDecimal("185.00"))
                    .quantidadeEstoque(80)
                    .dataValidade(LocalDate.now().plusMonths(18))
                    .fornecedor(f2)
                    .build());
        }

        if (clienteRepository.count() == 0) {
            clienteRepository.save(Cliente.builder()
                    .nome("Fazenda Boa Vista")
                    .cpfCnpj("33.444.555/0001-66")
                    .inscricaoEstadual("10987654-3")
                    .telefone("(62) 99999-1111")
                    .endereco("Rodovia GO-060, Km 45, Zona Rural")
                    .build());

            clienteRepository.save(Cliente.builder()
                    .nome("João Pedro Produtor")
                    .cpfCnpj("123.456.789-00")
                    .inscricaoEstadual("ISENTO")
                    .telefone("(62) 98888-2222")
                    .endereco("Estrada Municipal S/N, Chácara Esperança")
                    .build());
        }
    }
}

