package com.camsell.identity.infrastructure.security;

import com.camsell.identity.domain.model.UserRole;
import com.camsell.identity.domain.port.in.CurrentUserPort;
import com.camsell.shared.domain.exception.OperationNotAllowedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Unica implementacion que conoce el {@code SecurityContext}; el resto de los
 * modulos consume el puerto {@link CurrentUserPort}.
 */
@Component
public class SecurityContextCurrentUser implements CurrentUserPort {

    @Override
    public Optional<Long> currentUserId() {
        return jwt().map(token -> token.getClaim("uid"))
                .filter(Objects::nonNull)
                .map(value -> ((Number) value).longValue());
    }

    @Override
    public Long requireCurrentUserId() {
        return currentUserId().orElseThrow(
                () -> new OperationNotAllowedException("Debes iniciar sesion para realizar esta accion"));
    }

    @Override
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(UserRole.ADMIN.authority()::equals);
    }

    @Override
    public boolean canManage(Long ownerId) {
        if (isAdmin()) {
            return true;
        }
        return currentUserId().map(id -> id.equals(ownerId)).orElse(false);
    }

    private Optional<Jwt> jwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return Optional.of(token.getToken());
        }
        return Optional.empty();
    }
}
