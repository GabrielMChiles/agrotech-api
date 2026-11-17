package com.example.agrotech_api.externo;

import com.example.agrotech_api.exception.BusinessRuleException;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AnaliseCreditoServiceTest {

    private AnaliseCreditoGateway creditoGateway;
    private AnaliseCreditoService analiseCreditoService;

    public AnaliseCreditoServiceTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        creditoGateway = mock(AnaliseCreditoGateway.class);
        analiseCreditoService = new AnaliseCreditoService(creditoGateway);
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testValidarCreditoSafraAprovado() {
        String cpfProdutor = "123.456.789-00";
        BigDecimal valorCompraAdubo = new BigDecimal("8500.00");

        when(creditoGateway.consultarLimiteCredito(cpfProdutor, valorCompraAdubo)).thenReturn(true);

        assertDoesNotThrow(() -> {
            analiseCreditoService.validarCreditoSafra(cpfProdutor, valorCompraAdubo);
        });

        verify(creditoGateway, times(1)).consultarLimiteCredito(cpfProdutor, valorCompraAdubo);
    }

    @Test
    public void testValidarCreditoSafraReprovado() {
        String cpfProdutor = "123.456.789-00";
        BigDecimal valorCompraAdubo = new BigDecimal("25000.00");

        when(creditoGateway.consultarLimiteCredito(cpfProdutor, valorCompraAdubo)).thenReturn(false);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            analiseCreditoService.validarCreditoSafra(cpfProdutor, valorCompraAdubo);
        });

        assertTrue(exception.getMessage().contains("Crédito rural reprovado"));
        verify(creditoGateway, times(1)).consultarLimiteCredito(cpfProdutor, valorCompraAdubo);
    }
}
