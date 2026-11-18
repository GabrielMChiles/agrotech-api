package com.example.agrotech_api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FuncionarioRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres") String nome,
        @NotBlank(message = "A matrícula é obrigatória")
        @Size(max = 20, message = "A matrícula deve ter no máximo 20 caracteres") String matricula,
        @NotBlank(message = "O cargo é obrigatório")
        @Size(max = 50, message = "O cargo deve ter no máximo 50 caracteres") String cargo
) {}
