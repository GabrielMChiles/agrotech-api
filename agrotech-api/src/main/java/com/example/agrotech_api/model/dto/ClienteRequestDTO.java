package com.example.agrotech_api.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequestDTO(
        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "O CPF/CNPJ é obrigatório") String cpfCnpj,
        String inscricaoEstadual,
        String telefone,
        String endereco
) {}
