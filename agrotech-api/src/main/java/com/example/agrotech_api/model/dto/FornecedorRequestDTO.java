package com.example.agrotech_api.model.dto;

import com.example.agrotech_api.validation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorRequestDTO(
        @NotBlank(message = "A razão social é obrigatória")
        @Size(max = 100, message = "A razão social deve ter no máximo 100 caracteres") String razaoSocial,
        @NotBlank(message = "O CNPJ é obrigatório") @Cnpj String cnpj,
        @Telefone String telefoneContato
) {}
