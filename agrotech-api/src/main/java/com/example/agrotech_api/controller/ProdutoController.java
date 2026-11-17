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

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> cadastrar(@Valid @RequestBody ProdutoRequestDTO dto) {
        Produto salvo = produtoService.salvar(toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(salvo));
    }

    @GetMapping
    public ResponseEntity<Page<ProdutoResponseDTO>> listarTodos(@PageableDefault(size = 20) Pageable pageable) {
        Page<ProdutoResponseDTO> page = produtoService.listarTodos(pageable).map(this::toDTO);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(produtoService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody ProdutoRequestDTO dto) {
        Produto atualizado = produtoService.atualizar(id, toEntity(dto));
        return ResponseEntity.ok(toDTO(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private Produto toEntity(ProdutoRequestDTO dto) {
        return Produto.builder()
                .nome(dto.nome())
                .categoria(dto.categoria())
                .precoAtual(dto.precoAtual())
                .quantidadeEstoque(dto.quantidadeEstoque())
                .dataValidade(dto.dataValidade())
                .fornecedor(Fornecedor.builder().id(dto.fornecedorId()).build())
                .build();
    }

    private ProdutoResponseDTO toDTO(Produto p) {
        return new ProdutoResponseDTO(
                p.getId(),
                p.getNome(),
                p.getCategoria(),
                p.getPrecoAtual(),
                p.getQuantidadeEstoque(),
                p.getDataValidade(),
                p.getFornecedor().getId(),
                p.getFornecedor().getRazaoSocial()
        );
    }
}
