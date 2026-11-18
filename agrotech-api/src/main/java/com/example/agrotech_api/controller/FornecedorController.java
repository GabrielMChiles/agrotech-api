package com.example.agrotech_api.controller;

import com.example.agrotech_api.mapper.FornecedorMapper;
import com.example.agrotech_api.model.Fornecedor;
import com.example.agrotech_api.model.dto.FornecedorRequestDTO;
import com.example.agrotech_api.model.dto.FornecedorResponseDTO;
import com.example.agrotech_api.service.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {
    private final FornecedorService fornecedorService;
    private final FornecedorMapper fornecedorMapper;

    public FornecedorController(FornecedorService fornecedorService, FornecedorMapper fornecedorMapper) {
        this.fornecedorService = fornecedorService;
        this.fornecedorMapper = fornecedorMapper;
    }

    @PostMapping
    public ResponseEntity<FornecedorResponseDTO> cadastrar(@Valid @RequestBody FornecedorRequestDTO dto) {
        Fornecedor salvo = fornecedorService.salvar(fornecedorMapper.toEntity(dto));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(salvo.getId()).toUri();
        return ResponseEntity.created(location).body(fornecedorMapper.toResponseDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FornecedorResponseDTO> atualizar(@PathVariable Long id,
            @Valid @RequestBody FornecedorRequestDTO dto) {
        return ResponseEntity.ok(fornecedorMapper.toResponseDTO(fornecedorService.atualizar(id, fornecedorMapper.toEntity(dto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        fornecedorService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FornecedorResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(fornecedorMapper.toResponseDTO(fornecedorService.buscarPorId(id)));
    }

    @GetMapping
    public ResponseEntity<Page<FornecedorResponseDTO>> listarTodos(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(fornecedorService.listarTodos(pageable).map(fornecedorMapper::toResponseDTO));
    }
}