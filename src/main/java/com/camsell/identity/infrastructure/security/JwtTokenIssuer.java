package com.camsell.identity.infrastructure.security;

import com.camsell.identity.domain.model.User;
import com.camsell.identity.domain.port.out.TokenIssuerPort;
import com.camsell.shared.config.CamsellProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Emite los access tokens de la plataforma. */
@Component
public class JwtTokenIssuer implements TokenIssuerPort {

    private final JwtEncoder encoder;
    private final CamsellProperties properties;

    public JwtTokenIssuer(JwtEncoder encoder, CamsellProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    @Override
    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.jwt().issuer())
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenTtl()))
                .subject(user.email())
                .claim("uid", user.id())
                .claim("name", user.fullName())
                .claim("roles", List.of(user.role().authority()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public Duration accessTokenTtl() {
        Duration ttl = properties.jwt().accessTokenTtl();
        return ttl == null ? Duration.ofHours(12) : ttl;
    }
}
