package com.camsell.identity.domain.port.out;

import com.camsell.identity.domain.model.User;

import java.time.Duration;

/** Emision de credenciales de acceso. */
public interface TokenIssuerPort {

    String issueAccessToken(User user);

    Duration accessTokenTtl();
}
