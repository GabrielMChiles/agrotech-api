package com.example.agrotech_api.n2.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Testes de API REST - Agrotech (RBAC e Ciclo de Vida de Produto)")
public class TesteEndpoint {

    private static final String BASE_URL = "http://localhost:8080";
    private static String adminToken;
    private static String funcionarioToken;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;

        adminToken = autenticar("admin", "admin123");
        funcionarioToken = autenticar("ana", "ana123");
    }

    private static String autenticar(String username, String senha) {
        String loginPayload = """
            {
                "username": "%s",
                "senha": "%s"
            }
            """.formatted(username, senha);

        return given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/api/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Test
    @DisplayName("Caso 1 (Segurança RBAC): Deve retornar 403 Forbidden quando FUNCIONARIO (ana) tentar cadastrar usuário")
    public void testFuncionarioCriarUsuario() {
        String payloadNovoUsuario = """
            {
                "username": "invasor.agro",
                "senha": "invasor123",
                "role": "ADMIN",
                "funcionarioId": 1
            }
            """;

        given()
                .header("Authorization", "Bearer " + funcionarioToken)
                .contentType(ContentType.JSON)
                .body(payloadNovoUsuario)
                .when()
                .post("/api/usuarios")
                .then()
                .log().ifValidationFails()
                .statusCode(403);
    }

    @Test
    @DisplayName("Caso 2 (Persistência & Contrato): Deve criar um produto completo via POST e recuperá-lo via GET")
    public void testCriarEConsultarProdutoComSucesso() {
        Number fornecedorIdNumber = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/fornecedores")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .path("content[0].id");

        long fornecedorId = fornecedorIdNumber.longValue();
        String dataValidadeFutura = LocalDate.now().plusYears(1).toString();

        String payloadProduto = """
            {
                "nome": "Herbicida Seletivo Milho 20L",
                "categoria": "Defensivos",
                "precoAtual": 340.00,
                "quantidadeEstoque": 45,
                "dataValidade": "%s",
                "fornecedorId": %d
            }
            """.formatted(dataValidadeFutura, fornecedorId);

        Number produtoIdNumber = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(payloadProduto)
                .when()
                .post("/api/produtos")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("id", notNullValue())
                .body("nome", equalTo("Herbicida Seletivo Milho 20L"))
                .body("categoria", equalTo("Defensivos"))
                .extract()
                .path("id");

        long produtoIdGerado = produtoIdNumber.longValue();

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/produtos/" + produtoIdGerado)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("id", equalTo((int) produtoIdGerado))
                .body("quantidadeEstoque", equalTo(45))
                .body("precoAtual", equalTo(340.00f));
    }
}