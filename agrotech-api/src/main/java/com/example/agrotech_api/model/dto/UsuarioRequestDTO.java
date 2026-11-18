package com.example.agrotech_api.model.dto;

import com.example.agrotech_api.model.RoleUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
        @NotBlank(message = "O nome de usuário é obrigatório")
        @Size(min = 3, max = 60, message = "O login deve ter entre 3 e 60 caracteres")
        @Pattern(regexp = "^\\s*[A-Za-z0-9._-]+\\s*$", message = "O login aceita apenas letras, números, ponto, hífen e sublinhado")
        String username,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 4, max = 72, message = "A senha deve ter entre 4 e 72 caracteres") String senha,
        @NotNull(message = "O perfil (role) é obrigatório") RoleUsuario role,
        @NotNull(message = "O funcionário vinculado é obrigatório") Long funcionarioId
) {}
