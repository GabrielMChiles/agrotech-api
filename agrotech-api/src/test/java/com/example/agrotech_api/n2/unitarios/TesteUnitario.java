package com.example.agrotech_api.n2.unitarios;

import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.exception.InsufficientStockException;
import com.example.agrotech_api.model.*;
import com.example.agrotech_api.repository.ProdutoRepository;
import com.example.agrotech_api.repository.VendaRepository;
import com.example.agrotech_api.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TesteUnitario {

    @Mock
    private VendaRepository vendaRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private FuncionarioService funcionarioService;

    @Mock
    private FornecedorService fornecedorService;

    private ProdutoService produtoServiceReal;

    @Mock
    private ProdutoService produtoServiceMock;

    @InjectMocks
    private VendaService vendaService;

    @BeforeEach
    void setUp() {
        produtoServiceReal = new ProdutoService(produtoRepository, fornecedorService);
    }

    @Test
    void testDebitarEstoqueInsuficiente() {
        Produto adubo = new Produto();
        adubo.setNome("Adubo NPK 04-14-08");
        adubo.setQuantidadeEstoque(40);
        adubo.setPrecoAtual(new BigDecimal("150.00"));

        assertThrows(InsufficientStockException.class, () ->
                produtoServiceReal.debitarEstoque(adubo, 50)
        );

        assertEquals(40, adubo.getQuantidadeEstoque());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void testCancelarVendaEstornoEstoque() {
        Produto adubo = new Produto();
        adubo.setId(10L);
        adubo.setNome("Adubo NPK 04-14-08");
        adubo.setQuantidadeEstoque(40);

        ItemVenda item = new ItemVenda();
        item.setId(1L);
        item.setProduto(adubo);
        item.setQuantidade(50);
        item.setPrecoUnitarioNoMomentoDaVenda(new BigDecimal("150.00"));

        Venda venda = new Venda();
        venda.setId(1L);
        venda.setStatus(StatusVenda.CONCLUIDA);
        venda.setItens(List.of(item));

        when(vendaRepository.findByIdComDetalhesCompletos(1L)).thenReturn(Optional.of(venda));

        vendaService.cancelarVenda(1L);

        assertEquals(StatusVenda.CANCELADA, venda.getStatus());
        verify(produtoServiceMock, times(1)).creditarEstoque(adubo, 50);
        verify(vendaRepository, times(1)).save(venda);
    }

    @Test
    void testEfetivarVendaSemItens() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(funcionarioService.buscarPorId(1L)).thenReturn(funcionario);

        Venda vendaSemItens = new Venda();
        vendaSemItens.setCliente(cliente);
        vendaSemItens.setFuncionario(funcionario);
        vendaSemItens.setItens(new ArrayList<>());

        assertThrows(BusinessRuleException.class, () ->
                vendaService.efetivarVenda(vendaSemItens)
        );

        verify(vendaRepository, never()).save(any());
    }
}