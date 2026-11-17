package com.example.agrotech_api.model.dto;

public record LoginResponseDTO(
        String token,
        String username,
        String role,
        Long funcionarioId,
        String funcionarioNome
) {}

