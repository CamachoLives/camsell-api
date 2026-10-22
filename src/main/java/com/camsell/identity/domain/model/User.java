package com.camsell.identity.domain.model;

import java.time.Instant;

/** Usuario de la plataforma. El hash de clave nunca sale de este agregado. */
public record User(
        Long id,
        String email,
        String passwordHash,
        String fullName,
        String phone,
        String cityCode,
        String avatarUrl,
        UserRole role,
        boolean active,
        Instant createdAt) {

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isDealer() {
        return role == UserRole.DEALER;
    }

    public User withProfile(String newFullName, String newPhone, String newCityCode, String newAvatarUrl) {
        return new User(id, email, passwordHash, newFullName, newPhone, newCityCode, newAvatarUrl,
                role, active, createdAt);
    }

    public User withPasswordHash(String newHash) {
        return new User(id, email, newHash, fullName, phone, cityCode, avatarUrl, role, active, createdAt);
    }
}
