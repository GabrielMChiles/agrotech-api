package com.example.agrotech_api.model.dto;

public record FuncionarioResponseDTO(
        Long id, String nome, String matricula, String cargo
) {}