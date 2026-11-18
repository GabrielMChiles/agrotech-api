package com.example.agrotech_api.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VendaRequestDTO(
        @NotNull(message = "O ID do cliente é obrigatório")
        Long clienteId,

        // Opcional: o vendedor efetivo é o usuário autenticado
        Long funcionarioId,

        @NotEmpty(message = "A venda deve conter pelo menos um item")
        @Valid
        List<ItemVendaRequestDTO> itens
) {}




