package com.example.agrotech_api.n1.service;

import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.exception.InsufficientStockException;
import com.example.agrotech_api.model.Produto;
import com.example.agrotech_api.repository.ProdutoRepository;
import java.math.BigDecimal;

import com.example.agrotech_api.service.FornecedorService;
import com.example.agrotech_api.service.ProdutoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProdutoServiceTest {

    private ProdutoRepository produtoRepository;
    private FornecedorService fornecedorService;
    private ProdutoService produtoService;

    public ProdutoServiceTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        produtoRepository = mock(ProdutoRepository.class);
        fornecedorService = mock(FornecedorService.class);
        produtoService = new ProdutoService(produtoRepository, fornecedorService);
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testDebitarEstoqueInsuficiente() {
        Produto adubo = new Produto();
        adubo.setNome("Adubo NPK 04-14-08");
        adubo.setQuantidadeEstoque(40);
        adubo.setPrecoAtual(new BigDecimal("150.00"));

        assertThrows(InsufficientStockException.class, () -> {
            produtoService.debitarEstoque(adubo, 50);
        });

        assertEquals(40, adubo.getQuantidadeEstoque());
    }

    @Test
    public void testCreditarEstoque() {
        Produto adubo = new Produto();
        adubo.setNome("Adubo NPK 04-14-08");
        adubo.setQuantidadeEstoque(40);
        adubo.setPrecoAtual(new BigDecimal("150.00"));

        produtoService.creditarEstoque(adubo, 10);

        assertEquals(50, adubo.getQuantidadeEstoque());
    }

    @Test
    public void testSalvarProdutoSemFornecedor() {
        Produto defensivo = new Produto();
        defensivo.setNome("Herbicida Glifosato 20L");
        defensivo.setPrecoAtual(new BigDecimal("350.00"));
        defensivo.setQuantidadeEstoque(15);
        defensivo.setFornecedor(null);

        assertThrows(BusinessRuleException.class, () -> {
            produtoService.salvar(defensivo);
        });

        verify(produtoRepository, never()).save(defensivo);
    }
}
