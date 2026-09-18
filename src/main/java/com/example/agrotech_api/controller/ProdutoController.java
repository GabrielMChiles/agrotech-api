package com.example.agrotech_api.controller;

import com.example.agrotech_api.model.Fornecedor;
import com.example.agrotech_api.model.Produto;
import com.example.agrotech_api.model.dto.ProdutoRequestDTO;
import com.example.agrotech_api.model.dto.ProdutoResponseDTO;
import com.example.agrotech_api.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller REST de Produtos
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> cadastrar(@Valid @RequestBody ProdutoRequestDTO dto) {
        Produto produtoInput = Produto.builder()
                .nome(dto.nome())
                .precoAtual(dto.precoAtual())
                .quantidadeEstoque(dto.quantidadeEstoque())
                .fornecedor(Fornecedor.builder().id(dto.fornecedorId()).build())
                .build();

        Produto salvo = produtoService.salvar(produtoInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(salvo));
    }

    @GetMapping
    public ResponseEntity<Page<ProdutoResponseDTO>> listarTodos(@PageableDefault(size = 20) Pageable pageable) {
        Page<ProdutoResponseDTO> page = produtoService.listarTodos(pageable).map(this::toDTO);
        return ResponseEntity.ok(page);
    }

    private ProdutoResponseDTO toDTO(Produto p) {
        return new ProdutoResponseDTO(
                p.getId(), p.getNome(), p.getPrecoAtual(),
                p.getQuantidadeEstoque(), p.getFornecedor().getId(), p.getFornecedor().getRazaoSocial()
        );
    }
}