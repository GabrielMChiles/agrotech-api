package com.example.agrotech_api.controller;

import com.example.agrotech_api.mapper.FuncionarioMapper;
import com.example.agrotech_api.model.Funcionario;
import com.example.agrotech_api.model.dto.FuncionarioRequestDTO;
import com.example.agrotech_api.model.dto.FuncionarioResponseDTO;
import com.example.agrotech_api.service.FuncionarioService;
import com.example.agrotech_api.service.ProdutoService;
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
@RequestMapping("/api/funcionarios")
public class FuncionarioController {
    private final FuncionarioService funcionarioService;
    private final FuncionarioMapper funcionarioMapper;

    public FuncionarioController(FuncionarioService funcionarioService, FuncionarioMapper funcionarioMapper) {
        this.funcionarioService = funcionarioService;
        this.funcionarioMapper = funcionarioMapper;
    }

    @PostMapping
    public ResponseEntity<FuncionarioResponseDTO> cadastrar(@Valid @RequestBody FuncionarioRequestDTO dto) {
        Funcionario salvo = funcionarioService.salvar(funcionarioMapper.toEntity(dto));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(salvo.getId()).toUri();
        return ResponseEntity.created(location).body(funcionarioMapper.toResponseDTO(salvo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(funcionarioMapper.toResponseDTO(funcionarioService.buscarPorId(id)));
    }

    @GetMapping
    public ResponseEntity<Page<FuncionarioResponseDTO>> listarTodos(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(funcionarioService.listarTodos(pageable).map(funcionarioMapper::toResponseDTO));
    }
}