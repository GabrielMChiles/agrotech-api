package com.example.agrotech_api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProdutoRequestDTO(
        @NotBlank(message = "Nome é obrigatório") String nome,
        @NotNull(message = "Preço é obrigatório") @Positive BigDecimal precoAtual,
        @NotNull(message = "Estoque é obrigatório") @PositiveOrZero Integer quantidadeEstoque,
        @NotNull(message = "ID do Fornecedor é obrigatório") Long fornecedorId
) {}