package com.camsell.identity.application;

import com.camsell.identity.domain.model.User;
import com.camsell.identity.domain.model.UserRole;
import com.camsell.identity.domain.port.in.UserUseCase;
import com.camsell.identity.domain.port.out.PasswordHasherPort;
import com.camsell.identity.domain.port.out.UserRepositoryPort;
import com.camsell.shared.domain.exception.BusinessRuleException;
import com.camsell.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService implements UserUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;

    public UserService(UserRepositoryPort users, PasswordHasherPort hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    @Override
    public User updateProfile(Long id, UpdateProfileCommand command) {
        User user = getById(id);
        return users.save(user.withProfile(
                command.fullName() == null ? user.fullName() : command.fullName().trim(),
                command.phone(),
                command.cityCode(),
                command.avatarUrl()));
    }

    @Override
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = getById(id);
        if (!hasher.matches(currentPassword, user.passwordHash())) {
            throw new BusinessRuleException("invalid_credentials", "La clave actual no es correcta");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new BusinessRuleException("La nueva clave debe tener al menos 8 caracteres");
        }
        users.save(user.withPasswordHash(hasher.hash(newPassword)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return users.findAll();
    }

    @Override
    public User changeRole(Long id, UserRole role) {
        User user = getById(id);
        return users.save(new User(
                user.id(),
                user.email(),
                user.passwordHash(),
                user.fullName(),
                user.phone(),
                user.cityCode(),
                user.avatarUrl(),
                role,
                user.active(),
                user.createdAt()));
    }
}
