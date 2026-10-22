package com.camsell.identity.domain.port.in;

import com.camsell.identity.domain.model.User;
import com.camsell.identity.domain.model.UserRole;

public interface AuthenticationUseCase {

    AuthenticatedSession register(RegisterCommand command);

    AuthenticatedSession login(LoginCommand command);

    record RegisterCommand(
            String email,
            String password,
            String fullName,
            String phone,
            String cityCode,
            UserRole requestedRole) {
    }

    record LoginCommand(String email, String password) {
    }

    /** Resultado de una autenticacion exitosa. */
    record AuthenticatedSession(String accessToken, long expiresInSeconds, User user) {
    }
}
