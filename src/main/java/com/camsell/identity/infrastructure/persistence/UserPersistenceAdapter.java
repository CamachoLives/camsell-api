package com.camsell.identity.infrastructure.persistence;

import com.camsell.identity.domain.model.User;
import com.camsell.identity.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository repository;

    public UserPersistenceAdapter(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserEntity entity = user.id() == null
                ? new UserEntity()
                : repository.findById(user.id()).orElseGet(UserEntity::new);
        entity.setEmail(user.email());
        entity.setPasswordHash(user.passwordHash());
        entity.setFullName(user.fullName());
        entity.setPhone(user.phone());
        entity.setCityCode(user.cityCode());
        entity.setAvatarUrl(user.avatarUrl());
        entity.setRole(user.role());
        entity.setActive(user.active());
        return toDomain(repository.save(entity));
    }

    @Override
    public List<User> findAll() {
        return repository.findAllByOrderByFullNameAsc().stream().map(this::toDomain).toList();
    }

    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFullName(),
                entity.getPhone(),
                entity.getCityCode(),
                entity.getAvatarUrl(),
                entity.getRole(),
                entity.isActive(),
                entity.getCreatedAt());
    }
}
