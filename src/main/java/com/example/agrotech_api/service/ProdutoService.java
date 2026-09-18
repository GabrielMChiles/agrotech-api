package com.example.agrotech_api.service;

import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.exception.InsufficientStockException;
import com.example.agrotech_api.exception.ResourceNotFoundException;
import com.example.agrotech_api.model.Fornecedor;
import com.example.agrotech_api.model.Produto;
import com.example.agrotech_api.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final FornecedorService fornecedorService;

    public ProdutoService(ProdutoRepository produtoRepository, FornecedorService fornecedorService) {
        this.produtoRepository = produtoRepository;
        this.fornecedorService = fornecedorService;
    }

    @Transactional
    public Produto salvar(Produto produtoInput) {
        if (produtoInput.getFornecedor() == null || produtoInput.getFornecedor().getId() == null) {
            throw new BusinessRuleException("O produto deve possuir um fornecedor vinculado.");
        }
        Fornecedor fornecedor = fornecedorService.buscarPorId(produtoInput.getFornecedor().getId());
        produtoInput.setFornecedor(fornecedor);
        return produtoRepository.save(produtoInput);
    }

    @Transactional
    public Produto atualizar(Long id, Produto produtoAtualizado) {
        Produto produtoExistente = buscarPorId(id);
        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setPrecoAtual(produtoAtualizado.getPrecoAtual());
        produtoExistente.setQuantidadeEstoque(produtoAtualizado.getQuantidadeEstoque());
        return produtoRepository.save(produtoExistente);
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + id));
    }

    // REGRA DE NEGÓCIO RN02: Baixa de Estoque
    @Transactional
    public void debitarEstoque(Produto produto, Integer quantidade) {
        if (produto.getQuantidadeEstoque() < quantidade) {
            throw new InsufficientStockException("Estoque insuficiente para o produto '" + produto.getNome() +
                    "'. Saldo disponível: " + produto.getQuantidadeEstoque() + ", Solicitado: " + quantidade);
        }
        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidade);
        produtoRepository.save(produto);
    }

    // REGRA DE NEGÓCIO RN03: Estorno de Estoque no Cancelamento
    @Transactional
    public void creditarEstoque(Produto produto, Integer quantidade) {
        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + quantidade);
        produtoRepository.save(produto);
    }

    public Page<Produto> listarTodos(Pageable pageable) {
        return produtoRepository.findAll(pageable);
    }
}