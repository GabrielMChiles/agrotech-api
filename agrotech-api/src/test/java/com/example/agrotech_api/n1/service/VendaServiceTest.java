package com.example.agrotech_api.n1.service;

import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.model.Cliente;
import com.example.agrotech_api.model.Funcionario;
import com.example.agrotech_api.model.ItemVenda;
import com.example.agrotech_api.model.Produto;
import com.example.agrotech_api.model.StatusVenda;
import com.example.agrotech_api.model.Venda;
import com.example.agrotech_api.repository.VendaRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.agrotech_api.service.ClienteService;
import com.example.agrotech_api.service.FuncionarioService;
import com.example.agrotech_api.service.ProdutoService;
import com.example.agrotech_api.service.VendaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VendaServiceTest {

    private VendaRepository vendaRepository;
    private ClienteService clienteService;
    private FuncionarioService funcionarioService;
    private ProdutoService produtoService;
    private VendaService vendaService;

    public VendaServiceTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        vendaRepository = mock(VendaRepository.class);
        clienteService = mock(ClienteService.class);
        funcionarioService = mock(FuncionarioService.class);
        produtoService = mock(ProdutoService.class);
        vendaService = new VendaService(vendaRepository, clienteService, funcionarioService, produtoService);
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testCancelarVendaEstornoEstoque() {
        Produto adubo = new Produto();
        adubo.setId(10L);
        adubo.setNome("Adubo NPK 04-14-08");
        adubo.setQuantidadeEstoque(40);

        ItemVenda item = new ItemVenda();
        item.setId(1L);
        item.setProduto(adubo);
        item.setQuantidade(50);
        item.setPrecoUnitarioNoMomentoDaVenda(new BigDecimal("150.00"));

        List<ItemVenda> itens = new ArrayList<>();
        itens.add(item);

        Venda venda = new Venda();
        venda.setId(1L);
        venda.setStatus(StatusVenda.CONCLUIDA);
        venda.setItens(itens);

        when(vendaRepository.findByIdComDetalhesCompletos(1L)).thenReturn(Optional.of(venda));

        vendaService.cancelarVenda(1L);

        assertEquals(StatusVenda.CANCELADA, venda.getStatus());
        verify(produtoService).creditarEstoque(adubo, 50);
    }

    @Test
    public void testEfetivarVendaSemItens() {
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

        assertThrows(BusinessRuleException.class, () -> {
            vendaService.efetivarVenda(vendaSemItens);
        });

        verify(vendaRepository, never()).save(any());
    }
}
