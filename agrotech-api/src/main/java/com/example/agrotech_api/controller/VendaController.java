package com.example.agrotech_api.controller;

import com.example.agrotech_api.model.dto.VendaRequestDTO;
import com.example.agrotech_api.model.dto.VendaResponseDTO;
import com.example.agrotech_api.mapper.VendaMapper;
import com.example.agrotech_api.model.Venda;
import com.example.agrotech_api.service.VendaService;
import com.example.agrotech_api.config.AppSecurityConfig;
import com.example.agrotech_api.model.Funcionario;
import com.example.agrotech_api.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    private final VendaService vendaService;
    private final VendaMapper vendaMapper;

    public VendaController(VendaService vendaService, VendaMapper vendaMapper) {
        this.vendaService = vendaService;
        this.vendaMapper = vendaMapper;
    }

    @PostMapping
    public ResponseEntity<VendaResponseDTO> efetivarVenda(@Valid @RequestBody VendaRequestDTO requestDTO,
                                                         HttpServletRequest request) {
        Venda entityInput = vendaMapper.toEntity(requestDTO);
        // O vendedor é sempre o usuário autenticado; o funcionarioId do corpo é ignorado
        if (request.getAttribute(AppSecurityConfig.ATRIBUTO_USUARIO_LOGADO) instanceof Usuario logado) {
            entityInput.setFuncionario(Funcionario.builder().id(logado.getFuncionario().getId()).build());
        }
        Venda vendaEfetivada = vendaService.efetivarVenda(entityInput);
        VendaResponseDTO responseDTO = vendaMapper.toResponseDTO(vendaEfetivada);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(responseDTO.id())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<VendaResponseDTO>> listarTodas() {
        List<VendaResponseDTO> vendas = vendaService.listarTodas().stream()
                .map(vendaMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(vendas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaResponseDTO> buscarPorId(@PathVariable Long id) {
        Venda venda = vendaService.buscarPorIdComDetalhes(id);
        return ResponseEntity.ok(vendaMapper.toResponseDTO(venda));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarVenda(@PathVariable Long id) {
        vendaService.cancelarVenda(id);
        return ResponseEntity.noContent().build();
    }
}