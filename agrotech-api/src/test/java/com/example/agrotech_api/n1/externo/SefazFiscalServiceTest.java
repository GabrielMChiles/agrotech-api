package com.example.agrotech_api.n1.externo;

import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.externo.SefazFiscalGateway;
import com.example.agrotech_api.externo.SefazFiscalService;
import com.example.agrotech_api.model.Venda;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SefazFiscalServiceTest {

    private SefazFiscalGateway sefazGateway;
    private SefazFiscalService sefazFiscalService;

    public SefazFiscalServiceTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        sefazGateway = mock(SefazFiscalGateway.class);
        sefazFiscalService = new SefazFiscalService(sefazGateway);
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testEmitirNotaFiscalComSucesso() {
        Venda venda = new Venda();
        venda.setId(100L);
        venda.setValorTotal(new BigDecimal("1500.00"));

        when(sefazGateway.autorizarNotaFiscal(venda)).thenReturn("NFE-2026-AUTORIZADA-CHAVE-98765");

        String protocolo = sefazFiscalService.emitirNotaFiscal(venda);

        assertEquals("NFE-2026-AUTORIZADA-CHAVE-98765", protocolo);
        verify(sefazGateway, times(1)).autorizarNotaFiscal(venda);
    }

    @Test
    public void testEmitirNotaFiscalRejeicaoSefaz() {
        Venda venda = new Venda();
        venda.setId(101L);

        when(sefazGateway.autorizarNotaFiscal(venda))
                .thenThrow(new RuntimeException("Inscricao Estadual do Produtor irregular na SEFAZ"));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            sefazFiscalService.emitirNotaFiscal(venda);
        });

        assertTrue(exception.getMessage().contains("Falha na comunicação com a SEFAZ"));
    }
}
