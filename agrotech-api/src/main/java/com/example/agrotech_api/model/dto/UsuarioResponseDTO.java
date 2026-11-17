package com.example.agrotech_api.model.dto;

import com.example.agrotech_api.model.RoleUsuario;

public record UsuarioResponseDTO(
        Long id,
        String username,
        RoleUsuario role,
        Long funcionarioId,
        String funcionarioNome,
        String funcionarioMatricula
) {}

