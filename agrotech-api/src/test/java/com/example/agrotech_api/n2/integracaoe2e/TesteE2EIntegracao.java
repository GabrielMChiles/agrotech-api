package com.example.agrotech_api.n2.integracaoe2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes E2E (Selenium WebDriver) - Fluxos Operacionais AgroTech")
public class TesteE2EIntegracao {

    private static final String FRONTEND_URL = "http://localhost:4200";
    private static final String API_URL = "http://localhost:8080";
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        // Evita o popup "Change your password" do Chrome (senhas de teste fracas) durante o login
        options.addArguments("--disable-features=PasswordLeakDetection,SafetyCheck");
        java.util.Map<String, Object> prefs = new java.util.HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit(); // Regra 5.3: Liberação obrigatória do processo do navegador
        }
    }

    private void realizarLogin(String username, String senha) {
        driver.get(FRONTEND_URL + "/login");

        WebElement inputUser = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[formcontrolname='username'], input[name='username'], input[type='text']")
        ));
        WebElement inputSenha = driver.findElement(
                By.cssSelector("input[formcontrolname='senha'], input[name='senha'], input[type='password']")
        );
        WebElement btnEntrar = driver.findElement(By.cssSelector("button[type='submit']"));

        inputUser.clear();
        inputUser.sendKeys(username);
        inputSenha.clear();
        inputSenha.sendKeys(senha);
        btnEntrar.click();

        wait.until(ExpectedConditions.urlContains("/vendas"));
    }

    /**
     * Abre um componente PrimeNG <p-select> a partir do seu inputId e seleciona a primeira opção disponível.
     */
    private void selecionarPrimeiraOpcaoPrimeNG(String inputId) {
        WebElement containerSelect = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='" + inputId + "']/ancestor::*[contains(@class, 'p-select')][1]")
        ));
        abrirSelect(containerSelect);

        WebElement primeiraOpcao = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//li[@role='option' and not(@aria-disabled='true')])[1]")
        ));
        clicarComJs(primeiraOpcao);
        aguardarOverlayFechar();
    }

    /** Abre o p-select e garante que o overlay apareceu; se o primeiro clique se perder, tenta de novo. */
    private void abrirSelect(WebElement containerSelect) {
        for (int tentativa = 0; tentativa < 3; tentativa++) {
            containerSelect.click();
            try {
                new WebDriverWait(driver, Duration.ofSeconds(3)).until(
                        ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".p-select-overlay li[role='option']")));
                return;
            } catch (org.openqa.selenium.TimeoutException ignorado) {
                // overlay não abriu; tenta novamente
            }
        }
    }

    /** Clique via JS: o overlay do PrimeNG anima ao abrir e o clique nativo do Selenium pode se perder nesse intervalo. */
    private void clicarComJs(WebElement el) {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    /** Aguarda o overlay do p-select fechar, evitando clicar em opções de um dropdown anterior ainda em animação. */
    private void aguardarOverlayFechar() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".p-select-overlay")));
    }

    /** Cria via API um funcionário sem usuário vinculado, para o teste não depender de dados já consumidos. */
    private String criarFuncionarioLivre() {
        String token = given().baseUri(API_URL).contentType(ContentType.JSON)
                .body("{\"username\":\"admin\",\"senha\":\"admin123\"}")
                .post("/api/auth/login").then().statusCode(200).extract().path("token");

        String nome = "Operador E2E " + (System.currentTimeMillis() % 100000);
        given().baseUri(API_URL).contentType(ContentType.JSON).auth().oauth2(token)
                .body("{\"nome\":\"%s\",\"matricula\":\"E%d\",\"cargo\":\"Operador\"}"
                        .formatted(nome, System.currentTimeMillis() % 1000000))
                .post("/api/funcionarios").then().statusCode(201);
        return nome;
    }

    /**
     * Abre um componente PrimeNG <p-select> a partir do seu inputId e seleciona a opção pelo texto visível.
     */
    private void selecionarOpcaoPrimeNGPorTexto(String inputId, String textoOpcao) {
        WebElement containerSelect = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='" + inputId + "']/ancestor::*[contains(@class, 'p-select')][1]")
        ));
        abrirSelect(containerSelect);

        WebElement opcaoAlvo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[@role='option'][contains(., '" + textoOpcao + "')]")
        ));
        clicarComJs(opcaoAlvo);
        aguardarOverlayFechar();
    }

    @Test
    @DisplayName("Caso E2E 1: Funcionário (ana) deve logar, não ver menu Usuários e efetivar uma Venda")
    public void testEfetivarVenda() {
        // 1. Login como FUNCIONARIO (ana)
        realizarLogin("ana", "ana123");

        // 2. Valida RBAC na UI: link 'Usuários' não deve existir para FUNCIONARIO
        List<WebElement> linksUsuarios = driver.findElements(By.xpath("//a[contains(text(), 'Usuários')]"));
        assertTrue(linksUsuarios.isEmpty(), "Falha de RBAC na UI: Funcionário não deveria ver o menu Usuários.");

        // 3. Navega para 'Nova Venda' (/vendas/nova)
        WebElement linkNovaVenda = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Nova Venda')]")
        ));
        linkNovaVenda.click();
        wait.until(ExpectedConditions.urlContains("/vendas/nova"));

        // 4. Seleciona o Cliente e o Produto usando os inputIds exatos do NovaVendaComponent
        selecionarPrimeiraOpcaoPrimeNG("select-venda-cliente");
        selecionarPrimeiraOpcaoPrimeNG("select-venda-produto");

        // 5. Define quantidade = 2 no p-inputnumber (inputId="input-venda-quantidade")
        WebElement inputQuantidade = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("input-venda-quantidade")
        ));
        inputQuantidade.clear();
        inputQuantidade.sendKeys("2");

        // 6. Clica em 'Adicionar Item' (id="btn-adicionar-item")
        WebElement btnAdicionarItem = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#btn-adicionar-item button, #btn-adicionar-item")
        ));
        btnAdicionarItem.click();

        // 7. Aguarda o carrinho atualizar e clica em 'Finalizar Venda' (id="btn-finalizar-venda")
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//td[contains(text(), 'Nenhum item adicionado ao carrinho')]")
        ));

        WebElement btnFinalizarVenda = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#btn-finalizar-venda button, #btn-finalizar-venda")
        ));
        btnFinalizarVenda.click();

        // 8. Valida redirecionamento para o histórico (/vendas) após persistir no Back-end
        wait.until(ExpectedConditions.urlMatches(".*/vendas$"));
        assertTrue(driver.getCurrentUrl().endsWith("/vendas"), "Deveria redirecionar para o histórico de vendas.");
    }

    @Test
    @DisplayName("Caso E2E 2: Administrador (admin) deve acessar /usuarios e cadastrar novo acesso")
    public void testCadastrarNovoUsuario() {
        // 0. Garante um funcionário livre (sem usuário) para este teste
        String nomeFuncionario = criarFuncionarioLivre();

        // 1. Login como ADMIN (admin)
        realizarLogin("admin", "admin123");

        // 2. Clica no menu 'Usuários'
        WebElement linkUsuarios = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Usuários')]")
        ));
        linkUsuarios.click();
        wait.until(ExpectedConditions.urlContains("/usuarios"));

        // 3. Preenche username e senha usando os IDs exatos do UsuariosComponent
        String usernameDinamico = "operador." + (System.currentTimeMillis() % 10000);

        WebElement inputUsername = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("input-usuario-username")
        ));
        WebElement inputSenha = driver.findElement(By.id("input-usuario-senha"));

        inputUsername.clear();
        inputUsername.sendKeys(usernameDinamico);
        inputSenha.clear();
        inputSenha.sendKeys("senha123");

        // 4. Seleciona o perfil FUNCIONARIO e o funcionário livre criado no passo 0
        selecionarOpcaoPrimeNGPorTexto("select-usuario-role", "FUNCIONARIO");
        selecionarOpcaoPrimeNGPorTexto("select-usuario-funcionario", nomeFuncionario);

        // 5. Submete o formulário (id="btn-salvar-usuario")
        WebElement btnSalvarUsuario = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#btn-salvar-usuario button, #btn-salvar-usuario")
        ));
        btnSalvarUsuario.click();

        // 6. Valida o toast de sucesso na UI (a tabela é paginada, então o novo registro pode cair em outra página)
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(@class,'p-toast-summary')][contains(., 'Sucesso')]")
        ));

        // 7. Confirma a persistência consultando a API
        String token = given().baseUri(API_URL).contentType(ContentType.JSON)
                .body("{\"username\":\"admin\",\"senha\":\"admin123\"}")
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
        List<String> usernames = given().baseUri(API_URL).auth().oauth2(token)
                .get("/api/usuarios").then().statusCode(200).extract().path("username");
        assertTrue(usernames.contains(usernameDinamico), "O novo usuário cadastrado deveria existir na API.");
    }
}