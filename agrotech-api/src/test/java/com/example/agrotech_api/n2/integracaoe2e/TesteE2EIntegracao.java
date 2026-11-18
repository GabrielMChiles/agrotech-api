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

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes E2E (Selenium WebDriver) - Fluxos Operacionais AgroTech")
public class TesteE2EIntegracao {

    private static final String FRONTEND_URL = "http://localhost:4200";
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
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
     * Abre um componente PrimeNG <p-select> a partir do seu inputId,
     * aguarda o carregamento assíncrono das opções e seleciona o primeiro item,
     * garantindo o fechamento limpo do overlay antes da próxima ação.
     */
    private void selecionarPrimeiraOpcaoPrimeNG(String inputId) {
        // Pré-condição: garante que nenhum overlay anterior ainda esteja animando no DOM
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("li[role='option']"), 0));

        WebElement containerSelect = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='" + inputId + "']/ancestor::*[contains(@class, 'p-select')][1]")
        ));
        containerSelect.click();

        // Aguarda pelo menos 1 opção visível (sincroniza com o retorno do HttpClient)
        List<WebElement> opcoes = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("li[role='option']"))
        );
        opcoes.get(0).click();

        // Pós-condição: aguarda o overlay fechar completamente antes da próxima interação
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("li[role='option']"), 0));
    }

    /**
     * Abre um componente PrimeNG <p-select> a partir do seu inputId e seleciona a opção pelo texto visível.
     */
    private void selecionarOpcaoPrimeNGPorTexto(String inputId, String textoOpcao) {
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("li[role='option']"), 0));

        WebElement containerSelect = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='" + inputId + "']/ancestor::*[contains(@class, 'p-select')][1]")
        ));
        containerSelect.click();

        WebElement opcaoAlvo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[@role='option'][contains(., '" + textoOpcao + "')]")
        ));
        opcaoAlvo.click();

        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("li[role='option']"), 0));
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

        // 5. Define quantidade = 2 no p-inputnumber usando Ctrl+A para sobrescrever o valor padrão
        WebElement inputQuantidade = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("input-venda-quantidade")
        ));
        inputQuantidade.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
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

        // 4. Seleciona o perfil FUNCIONARIO e o funcionário livre 'Marcos Oliveira' (M03)
        selecionarOpcaoPrimeNGPorTexto("select-usuario-role", "FUNCIONARIO");
        selecionarOpcaoPrimeNGPorTexto("select-usuario-funcionario", "Marcos Oliveira");

        // 5. Submete o formulário (id="btn-salvar-usuario")
        WebElement btnSalvarUsuario = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#btn-salvar-usuario button, #btn-salvar-usuario")
        ));
        btnSalvarUsuario.click();

        // 6. Valida que o novo usuário apareceu na listagem da tabela
        WebElement registroNaTabela = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[contains(text(), '" + usernameDinamico + "')]")
        ));
        assertTrue(registroNaTabela.isDisplayed(), "O novo usuário cadastrado deveria aparecer na tabela.");
    }
}