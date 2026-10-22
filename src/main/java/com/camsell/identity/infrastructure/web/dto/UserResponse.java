package com.camsell.identity.infrastructure.web.dto;

import com.camsell.identity.domain.model.User;
import com.camsell.identity.domain.model.UserRole;

import java.time.Instant;

/** Proyeccion publica de un usuario: nunca incluye el hash de la clave. */
public record UserResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        String cityCode,
        String avatarUrl,
        UserRole role,
        boolean active,
        Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.id(),
                user.email(),
                user.fullName(),
                user.phone(),
                user.cityCode(),
                user.avatarUrl(),
                user.role(),
                user.active(),
                user.createdAt());
    }
}
