package com.camsell.identity.infrastructure.web.dto;

import com.camsell.identity.domain.port.in.AuthenticationUseCase;

public record SessionResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) {

    public static SessionResponse from(AuthenticationUseCase.AuthenticatedSession session) {
        return new SessionResponse(
                session.accessToken(),
                "Bearer",
                session.expiresInSeconds(),
                UserResponse.from(session.user()));
    }
}
