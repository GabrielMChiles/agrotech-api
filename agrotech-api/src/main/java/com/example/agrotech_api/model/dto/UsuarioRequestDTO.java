package com.example.agrotech_api.model.dto;

import com.example.agrotech_api.model.RoleUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
        @NotBlank(message = "O nome de usuário é obrigatório") String username,
        @NotBlank(message = "A senha é obrigatória") @Size(min = 4, message = "A senha deve ter ao menos 4 caracteres") String senha,
        @NotNull(message = "O perfil (role) é obrigatório") RoleUsuario role,
        @NotNull(message = "O funcionário vinculado é obrigatório") Long funcionarioId
) {}

