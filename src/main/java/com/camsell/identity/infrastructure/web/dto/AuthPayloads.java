package com.camsell.identity.infrastructure.web.dto;

import com.camsell.identity.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthPayloads {

    private AuthPayloads() {
    }

    public record RegisterPayload(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato valido")
            @Size(max = 180)
            String email,

            @NotBlank(message = "La clave es obligatoria")
            @Size(min = 8, max = 72, message = "La clave debe tener entre 8 y 72 caracteres")
            String password,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 160)
            String fullName,

            @Size(max = 40)
            String phone,

            @Size(max = 60)
            String cityCode,

            UserRole role) {
    }

    public record LoginPayload(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato valido")
            String email,

            @NotBlank(message = "La clave es obligatoria")
            String password) {
    }

    public record UpdateProfilePayload(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 160)
            String fullName,

            @Size(max = 40)
            String phone,

            @Size(max = 60)
            String cityCode,

            @Size(max = 400)
            String avatarUrl) {
    }

    public record ChangePasswordPayload(
            @NotBlank(message = "La clave actual es obligatoria")
            String currentPassword,

            @NotBlank(message = "La nueva clave es obligatoria")
            @Size(min = 8, max = 72, message = "La clave debe tener entre 8 y 72 caracteres")
            String newPassword) {
    }
}
