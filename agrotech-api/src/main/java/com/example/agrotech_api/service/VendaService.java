package com.example.agrotech_api.service;

import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.exception.ResourceNotFoundException;
import com.example.agrotech_api.model.*;
import com.example.agrotech_api.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteService clienteService;
    private final FuncionarioService funcionarioService;
    private final ProdutoService produtoService;

    public VendaService(VendaRepository vendaRepository,
                        ClienteService clienteService,
                        FuncionarioService funcionarioService,
                        ProdutoService produtoService) {
        this.vendaRepository = vendaRepository;
        this.clienteService = clienteService;
        this.funcionarioService = funcionarioService;
        this.produtoService = produtoService;
    }

    @Transactional
    public Venda efetivarVenda(Venda vendaRequisitada) {
        Cliente cliente = clienteService.buscarPorId(vendaRequisitada.getCliente().getId());
        Funcionario funcionario = funcionarioService.buscarPorId(vendaRequisitada.getFuncionario().getId());

        if (vendaRequisitada.getItens() == null || vendaRequisitada.getItens().isEmpty()) {
            throw new BusinessRuleException("Uma venda não pode ser efetivada sem pelo menos um item.");
        }

        Venda novaVenda = Venda.builder()
                .cliente(cliente)
                .funcionario(funcionario)
                .dataHoraVenda(LocalDateTime.now())
                .status(StatusVenda.CONCLUIDA)
                .valorTotal(BigDecimal.ZERO)
                .build();

        BigDecimal acumuladorTotal = BigDecimal.ZERO;

        for (ItemVenda itemSolicitado : vendaRequisitada.getItens()) {
            Produto produto = produtoService.buscarPorId(itemSolicitado.getProduto().getId());

            if (produto.getDataValidade() != null && produto.getDataValidade().isBefore(LocalDate.now())) {
                throw new BusinessRuleException("O produto '" + produto.getNome() + "' está vencido e não pode ser vendido.");
            }

            // RN02: Restrição de Estoque Mínimo
            produtoService.debitarEstoque(produto, itemSolicitado.getQuantidade());

            // RN01: Congelamento Histórico do Preço
            ItemVenda itemProcessado = ItemVenda.builder()
                    .produto(produto)
                    .quantidade(itemSolicitado.getQuantidade())
                    .precoUnitarioNoMomentoDaVenda(produto.getPrecoAtual())
                    .build();

            BigDecimal subtotal = itemProcessado.getPrecoUnitarioNoMomentoDaVenda()
                    .multiply(BigDecimal.valueOf(itemProcessado.getQuantidade()));

            acumuladorTotal = acumuladorTotal.add(subtotal);
            novaVenda.addItemVenda(itemProcessado);
        }

        novaVenda.setValorTotal(acumuladorTotal);

        Venda vendaSalva = vendaRepository.save(novaVenda);

        // Força a re-busca com JOIN FETCH ou inicialização antes de fechar a transação
        // impedindo o LazyInitializationException ao mapear no Controller
        return buscarPorIdComDetalhes(vendaSalva.getId());
    }

    @Transactional
    public void cancelarVenda(Long vendaId) {
        Venda venda = buscarPorIdComDetalhes(vendaId);

        // RN04: Imutabilidade de Venda Cancelada
        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new BusinessRuleException("A venda de ID " + vendaId + " já se encontra cancelada.");
        }

        // RN03: Estorno no Cancelamento
        for (ItemVenda item : venda.getItens()) {
            produtoService.creditarEstoque(item.getProduto(), item.getQuantidade());
        }

        venda.setStatus(StatusVenda.CANCELADA);
        vendaRepository.save(venda);
    }

    public Venda buscarPorIdComDetalhes(Long id) {
        return vendaRepository.findByIdComDetalhesCompletos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com o ID: " + id));
    }

    public List<Venda> listarTodas() {
        return vendaRepository.findAllComDetalhesCompletos();
    }
}
