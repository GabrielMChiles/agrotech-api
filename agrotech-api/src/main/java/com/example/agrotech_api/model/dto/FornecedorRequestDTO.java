package com.example.agrotech_api.model.dto;

import jakarta.validation.constraints.NotBlank;

public record FornecedorRequestDTO(
        @NotBlank(message = "A razão social é obrigatória") String razaoSocial,
        @NotBlank(message = "O CNPJ é obrigatório") String cnpj,
        String telefoneContato
) {}