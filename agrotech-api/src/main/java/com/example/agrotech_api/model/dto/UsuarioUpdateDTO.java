package com.example.agrotech_api.model.dto;

import com.example.agrotech_api.model.RoleUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Edição de usuário. O funcionário vinculado não muda; a senha é opcional
 * (em branco/nula mantém a senha atual).
 */
public record UsuarioUpdateDTO(
        @NotBlank(message = "O nome de usuário é obrigatório")
        @Size(min = 3, max = 60, message = "O login deve ter entre 3 e 60 caracteres")
        @Pattern(regexp = "^\\s*[A-Za-z0-9._-]+\\s*$", message = "O login aceita apenas letras, números, ponto, hífen e sublinhado")
        String username,
        @Pattern(regexp = "^$|^.{4,72}$", message = "A nova senha deve ter entre 4 e 72 caracteres")
        String senha,
        @NotNull(message = "O perfil (role) é obrigatório") RoleUsuario role
) {}
