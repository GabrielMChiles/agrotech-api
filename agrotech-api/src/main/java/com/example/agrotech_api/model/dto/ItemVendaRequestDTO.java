package com.example.agrotech_api.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemVendaRequestDTO(
        @NotNull(message = "O ID do produto é obrigatório")
        Long produtoId,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        @Max(value = 1_000_000, message = "A quantidade deve ser no máximo 1.000.000")
        Integer quantidade
) {}