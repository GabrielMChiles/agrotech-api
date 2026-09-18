package com.example.agrotech_api.model.dto;

import jakarta.validation.constraints.NotBlank;

public record FuncionarioRequestDTO(
        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "A matrícula é obrigatória") String matricula,
        @NotBlank(message = "O cargo é obrigatório") String cargo
) {}