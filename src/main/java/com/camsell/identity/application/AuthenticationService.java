package com.camsell.identity.application;

import com.camsell.identity.domain.model.User;
import com.camsell.identity.domain.model.UserRole;
import com.camsell.identity.domain.port.in.AuthenticationUseCase;
import com.camsell.identity.domain.port.out.PasswordHasherPort;
import com.camsell.identity.domain.port.out.TokenIssuerPort;
import com.camsell.identity.domain.port.out.UserRepositoryPort;
import com.camsell.shared.domain.exception.BusinessRuleException;
import com.camsell.shared.domain.exception.DuplicatedResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class AuthenticationService implements AuthenticationUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final TokenIssuerPort tokens;

    public AuthenticationService(UserRepositoryPort users, PasswordHasherPort hasher, TokenIssuerPort tokens) {
        this.users = users;
        this.hasher = hasher;
        this.tokens = tokens;
    }

    @Override
    public AuthenticatedSession register(RegisterCommand command) {
        String email = normalizeEmail(command.email());
        if (users.existsByEmail(email)) {
            throw new DuplicatedResourceException("Ya existe una cuenta registrada con " + email);
        }
        // El rol ADMIN solo se asigna desde la administracion, nunca en el alta publica.
        UserRole role = command.requestedRole() == null || command.requestedRole() == UserRole.ADMIN
                ? UserRole.USER
                : command.requestedRole();

        User saved = users.save(new User(
                null,
                email,
                hasher.hash(command.password()),
                command.fullName().trim(),
                command.phone(),
                command.cityCode(),
                null,
                role,
                true,
                null));
        log.info("Usuario registrado id={} rol={}", saved.id(), saved.role());
        return session(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedSession login(LoginCommand command) {
        String email = normalizeEmail(command.email());
        User user = users.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("invalid_credentials", "Correo o clave incorrectos"));
        if (!hasher.matches(command.password(), user.passwordHash())) {
            throw new BusinessRuleException("invalid_credentials", "Correo o clave incorrectos");
        }
        if (!user.active()) {
            throw new BusinessRuleException("account_disabled", "La cuenta se encuentra deshabilitada");
        }
        return session(user);
    }

    private AuthenticatedSession session(User user) {
        return new AuthenticatedSession(
                tokens.issueAccessToken(user),
                tokens.accessTokenTtl().toSeconds(),
                user);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessRuleException("El correo es obligatorio");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
