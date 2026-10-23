package com.camsell.shared.api;

import com.camsell.shared.config.CamsellProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Limita la tasa de peticiones por cliente.
 *
 * <p>El inicio de sesion tiene una cuota mucho mas estrecha que el resto: sin
 * ella, probar claves contra una cuenta conocida no tiene coste. Las lecturas
 * no se limitan porque ya se sirven con cache y revalidacion.
 */
@Component
@Order(2)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final String[] CREDENTIAL_PATHS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/me/password"
    };

    private final RateLimiter credentialLimiter;
    private final RateLimiter writeLimiter;
    private final boolean enabled;

    public RateLimitFilter(CamsellProperties properties) {
        CamsellProperties.RateLimit config = properties.rateLimit();
        this.enabled = config.enabled();
        this.credentialLimiter = new RateLimiter(config.credentialAttemptsPerMinute(), Duration.ofMinutes(1));
        this.writeLimiter = new RateLimiter(config.writesPerMinute(), Duration.ofMinutes(1));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        RateLimiter limiter = limiterFor(request);
        if (limiter == null) {
            chain.doFilter(request, response);
            return;
        }

        String key = clientKey(request);
        long retryAfterSeconds = limiter.tryConsume(key);
        if (retryAfterSeconds == 0) {
            chain.doFilter(request, response);
            return;
        }

        log.warn("Peticion frenada por limite de tasa: {} {}", request.getMethod(), request.getRequestURI());
        rejectWithProblemDetail(request, response, retryAfterSeconds);
    }

    private RateLimiter limiterFor(HttpServletRequest request) {
        if (!enabled) {
            return null;
        }
        String path = request.getRequestURI();
        for (String credentialPath : CREDENTIAL_PATHS) {
            if (path.equals(credentialPath)) {
                return credentialLimiter;
            }
        }
        return isWrite(request.getMethod()) ? writeLimiter : null;
    }

    private boolean isWrite(String method) {
        return switch (method) {
            case "POST", "PUT", "PATCH", "DELETE" -> true;
            default -> false;
        };
    }

    /**
     * Identifica al cliente. Se respeta {@code X-Forwarded-For} porque en
     * despliegue la API corre detras de un proxy y, sin eso, todos los clientes
     * compartirian la misma cubeta.
     */
    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() == null ? "desconocido" : request.getRemoteAddr();
    }

    private void rejectWithProblemDetail(
            HttpServletRequest request, HttpServletResponse response, long retryAfterSeconds)
            throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));

        // Se escribe a mano para no depender del serializador: este filtro corre
        // antes de que Spring MVC resuelva el controlador.
        String body = """
                {"type":"https://camsell.dev/problems/rate-limited",\
                "title":"Demasiadas peticiones",\
                "status":429,\
                "detail":"Superaste el limite de peticiones. Intenta de nuevo en %d segundos.",\
                "instance":"%s",\
                "code":"rate_limited",\
                "retryAfterSeconds":%d,\
                "timestamp":"%s"}"""
                .formatted(retryAfterSeconds, request.getRequestURI(), retryAfterSeconds, Instant.now());

        response.getWriter().write(body);
    }

    /** Punto de entrada para las pruebas, que necesitan empezar de cero. */
    public void resetAll() {
        credentialLimiter.resetAll();
        writeLimiter.resetAll();
    }
}
