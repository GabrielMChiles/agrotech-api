package com.example.agrotech_api.model.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres") String nome,
        @Size(max = 60, message = "A categoria deve ter no máximo 60 caracteres") String categoria,
        @NotNull(message = "Preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        @Digits(integer = 13, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais") BigDecimal precoAtual,
        @NotNull(message = "Estoque é obrigatório")
        @PositiveOrZero(message = "O estoque não pode ser negativo")
        @Max(value = 1_000_000, message = "O estoque deve ser no máximo 1.000.000") Integer quantidadeEstoque,
        LocalDate dataValidade,
        @NotNull(message = "ID do Fornecedor é obrigatório") Long fornecedorId
) {}
