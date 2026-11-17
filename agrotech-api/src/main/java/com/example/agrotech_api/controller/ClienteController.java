package com.example.agrotech_api.controller;

import com.example.agrotech_api.mapper.ClienteMapper;
import com.example.agrotech_api.model.Cliente;
import com.example.agrotech_api.model.dto.ClienteRequestDTO;
import com.example.agrotech_api.model.dto.ClienteResponseDTO;
import com.example.agrotech_api.service.ClienteService;
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
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clienteService;
    private final ClienteMapper clienteMapper;

    public ClienteController(ClienteService clienteService, ClienteMapper clienteMapper) {
        this.clienteService = clienteService;
        this.clienteMapper = clienteMapper;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> cadastrar(@Valid @RequestBody ClienteRequestDTO dto) {
        Cliente salvo = clienteService.salvar(clienteMapper.toEntity(dto));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(salvo.getId()).toUri();
        return ResponseEntity.created(location).body(clienteMapper.toResponseDTO(salvo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteMapper.toResponseDTO(clienteService.buscarPorId(id)));
    }

    @GetMapping
    public ResponseEntity<Page<ClienteResponseDTO>> listarTodos(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(clienteService.listarTodos(pageable).map(clienteMapper::toResponseDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizar(@PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO dto) {
        Cliente atualizado = clienteService.atualizar(id, clienteMapper.toEntity(dto));
        return ResponseEntity.ok(clienteMapper.toResponseDTO(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}