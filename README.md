# 🌱 AgroTech — Sistema de Gestão Comercial e Controle de Estoque de Insumos Agrícolas

> Plataforma Full Stack corporativa desenvolvida para o gerenciamento integrado de vendas, controle de estoque transacional, catálogo de insumos agrícolas, carteira de clientes e controle de acesso baseado em papéis.

---

## 👥 Integrantes do Grupo

| Nome Completo | Responsabilidade Principal |
| :--- | :--- |
| **Gabriel Machado Chiles** | Arquitetura Back-end (Spring Boot), Persistência (JPA/PostgreSQL), Regras de Venda/Clientes e Suíte de Testes |
| **Pabline Pereira dos Santos** | Segurança, Gestão de Produtos/Fornecedores, Design System e Front-end SPA (Angular + PrimeNG) |

---

## 📋 Descrição da Proposta

O **AgroTech** resolve um problema crítico de lojas agropecuárias e distribuidoras de insumos rurais: a falta de rastreabilidade entre o saldo físico em estoque, o congelamento histórico de preços no momento do faturamento e a segregação de privilégios operacionais.

O sistema opera sob uma arquitetura cliente-servidor estritamente desacoplada:

- **Regras de Negócio e Integridade (Back-end):**
  - **RN01 — Congelamento Histórico de Preço:** ao efetivar uma venda, o preço unitário vigente do insumo é persistido na tabela associativa (`TB_ITEM_VENDA`), impedindo que reajustes futuros alterem o histórico financeiro de pedidos já concluídos.
  - **RN02 — Baixa Atômica e Restrição de Estoque:** o faturamento valida o saldo disponível e debita o estoque dentro de uma única transação ACID (`@Transactional`). Tentativas de venda acima do saldo retornam `409 Conflict`, seguindo o padrão **RFC 7807 (Problem Details)**.
  - **RN03 — Estorno em Cancelamento:** o cancelamento de uma venda recompõe automaticamente o saldo físico dos produtos no estoque.
  - **RN04 — Imutabilidade de Venda Cancelada:** vendas canceladas não podem ser canceladas novamente (`422 Unprocessable Entity`).
  - **Segurança & RBAC (OWASP A01):** autenticação com hash **BCrypt**, gerenciamento de sessões ativas em memória no servidor e interceptação na borda HTTP (`HandlerInterceptor`), garantindo que apenas usuários com perfil `ADMIN` acessem endpoints administrativos (`/api/usuarios`).

---

## 🎬 Demonstrações em Vídeo

Abaixo estão os vídeos de validação funcional e técnica da solução:

| Escopo da Demonstração | Descrição | Link de Acesso |
| :--- | :--- | :--- |
| **Sistema Full Stack em Operação e Execução da Suíte de Testes** | Fluxo completo de autenticação (`ADMIN` e `FUNCIONARIO`), gestão de catálogo, clientes, controle de estoque e efetivação de vendas na SPA Angular integrada ao PostgreSQL. Também, execução automatizada da Pirâmide de Testes: Testes Unitários (`JUnit 5 + Mockito`), Testes de Contrato/RBAC na API (`REST-assured`) e Testes End-to-End no navegador (`Selenium WebDriver`). | [▶️ Assistir Demonstração do Sistema](https://canva.link/np6q9txfor89v2v) |

---

## 🛠️ Tecnologias Utilizadas

### Back-end (`agrotech-api`)

- **Java 21** (Records, Streams, Pattern Matching, `java.time`, `BigDecimal`)
- **Spring Boot 4.1.1** (Spring Web MVC, Spring Data JPA, Bean Validation, Spring Security Crypto)
- **PostgreSQL** (banco de dados relacional)
- **Lombok** (redução de boilerplate)

### Front-end (`agrotech-front`)

- **Angular 19+** (Standalone Components, Signals, Control Flow `@if` / `@for`, Reactive Forms, Lazy Loading)
- **TypeScript** (tipagem estrita)
- **PrimeNG (Tema Aura)** & **PrimeIcons** (Design System e componentes acessíveis)

### Qualidade e Automação de Testes (`N2`)

- **JUnit 5 & Mockito** (testes unitários isolados da camada de serviço)
- **REST-assured 5.5.1 & Hamcrest** (testes de contrato, persistência e segurança RBAC na API REST)
- **Selenium WebDriver 4.27+** (testes End-to-End automatizados no navegador Google Chrome)

---

## 📁 Estrutura do Repositório

```text
agrotech/
├── agrotech-api/          # API RESTful em Java + Spring Boot, entidades JPA, regras de negócio e suíte de testes
├── agrotech-front/        # Aplicação SPA em Angular + PrimeNG
├── docs/                  # Documentação de requisitos e evidências
└── README.md              # Documentação principal do projeto
```

---

## ⚙️ Instruções para Execução do Ambiente

### Pré-requisitos

- **JDK 21** (ou superior) e **Maven 3.9+**
- **Node.js 20 LTS+** e **Angular CLI** (`npm install -g @angular/cli`)
- **PostgreSQL 15+** em execução localmente
- **Google Chrome** instalado (para execução dos testes E2E com Selenium)

### 1. Configuração do Banco de Dados PostgreSQL

Crie um banco de dados vazio no PostgreSQL chamado `agrotech_db`:

```sql
CREATE DATABASE agrotech_db;
```

### 2. Configuração e Execução do Back-end (`agrotech-api`)

1. Abra o arquivo `agrotech-api/src/main/resources/application.properties`.
2. **Altere manualmente as credenciais do PostgreSQL** (`spring.datasource.username` e `spring.datasource.password`) conforme a configuração local da sua máquina:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/agrotech_db
   spring.datasource.username=postgres
   spring.datasource.password=SUA_SENHA_DO_POSTGRES_AQUI
   spring.jpa.hibernate.ddl-auto=update
   ```

3. Navegue até a pasta da API e inicie a aplicação:

   ```bash
   cd agrotech-api
   ./mvnw spring-boot:run
   ```

> **Carga Inicial Automática (`DatabaseSeeder`):** ao iniciar pela primeira vez, a aplicação cria automaticamente fornecedores, produtos, clientes, funcionários e dois usuários padrão com senhas criptografadas em BCrypt:
>
> - **Administrador (`ADMIN`):** usuário `admin` | senha `admin123`
> - **Operadora (`FUNCIONARIO`):** usuário `ana` | senha `ana123`

A API estará disponível em: `http://localhost:8080`.

### 3. Configuração e Execução do Front-end (`agrotech-front`)

Em um novo terminal, navegue até a pasta do front-end, instale as dependências e inicie o servidor de desenvolvimento:

```bash
cd agrotech-front
npm install
ng serve
```

Acesse a aplicação no navegador em: `http://localhost:4200`.

---

## 🧪 Suíte de Testes Automatizados

O projeto possui cobertura automatizada em três níveis da pirâmide de testes (localizados em `agrotech-api/src/test/java/com/example/agrotech_api/n2/`).

### 1. Testes Unitários (JUnit 5 + Mockito)

Validam as regras de negócio da camada `Service` de forma isolada, sem dependência de banco de dados ou rede:

- **Caso 1:** efetivação de venda válida com cálculo exato de subtotal (`BigDecimal`) e baixa de estoque (`RN01` e `RN02`).
- **Caso 2:** bloqueio com `InsufficientStockException` quando a quantidade solicitada excede o saldo disponível (`RN02`).
- **Caso 3:** cancelamento de venda concluída com estorno automático de estoque e bloqueio de cancelamento duplicado (`RN03` e `RN04`).

### 2. Testes de API REST (REST-assured)

Validam contratos HTTP, persistência no PostgreSQL e blindagem de segurança na borda *(requerem o Back-end em execução na porta `8080`)*:

- **Caso 1 (Segurança OWASP A01 — RBAC):** garante que uma requisição `POST /api/usuarios` autenticada com token de perfil `FUNCIONARIO` (`ana`) seja barrada pelo servidor com status **`403 Forbidden`**.
- **Caso 2 (Persistência & Contrato):** realiza o cadastro completo de um insumo agrícola via `POST /api/produtos` (`201 Created`) e valida sua recuperação consistente via `GET /api/produtos/{id}` (`200 OK`).

### 3. Testes End-to-End / E2E (Selenium WebDriver)

Simulam jornadas reais de usuários no navegador Google Chrome *(requerem Back-end na porta `8080` e Front-end na porta `4200`)*:

- **Caso E2E 1 (Fluxo Operacional):** autentica como `FUNCIONARIO` (`ana`), verifica que o menu administrativo `Usuários` está oculto na interface, navega até `/vendas/nova`, seleciona cliente e produto via componentes PrimeNG, adiciona itens ao carrinho e finaliza a venda.
- **Caso E2E 2 (Fluxo Administrativo):** autentica como `ADMIN` (`admin`), acessa `/usuarios`, preenche o formulário vinculando um funcionário disponível e valida a persistência e renderização imediata do novo usuário na tabela.

#### Como executar todos os testes via terminal

```bash
cd agrotech-api
./mvnw test
```

---