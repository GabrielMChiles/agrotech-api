package com.example.agrotech_api.model.dto;

import com.example.agrotech_api.validation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres") String nome,
        @NotBlank(message = "O CPF/CNPJ é obrigatório") @CpfCnpj String cpfCnpj,
        @Size(max = 50, message = "A inscrição estadual deve ter no máximo 50 caracteres") String inscricaoEstadual,
        @Telefone String telefone,
        @Size(max = 200, message = "O endereço deve ter no máximo 200 caracteres") String endereco
) {}
