package com.camsell.shared.api;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Propaga un identificador de correlacion y mide la latencia de cada peticion.
 *
 * <p>El identificador viaja al MDC, de modo que las lineas de log de una misma
 * peticion se pueden agrupar, y vuelve en la respuesta para que un reporte de
 * error pueda citarlo.
 *
 * <p>Las peticiones que superan el umbral se registran con nivel WARN: sin eso,
 * una consulta que se degrada solo se nota cuando alguien se queja.
 */
@Component
@Order(1)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";

    private static final Logger log = LoggerFactory.getLogger(RequestCorrelationFilter.class);
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_METHOD = "httpMethod";
    private static final String MDC_PATH = "httpPath";
    private static final String MDC_STATUS = "httpStatus";
    private static final String MDC_DURATION = "durationMs";

    /** Por encima de esto una peticion merece revisarse. */
    private static final long SLOW_REQUEST_MILLIS = 800;

    private final Counter slowRequests;

    public RequestCorrelationFilter(MeterRegistry registry) {
        this.slowRequests = Counter.builder("camsell.http.slow_requests")
                .description("Peticiones que superaron el umbral de latencia")
                .tag("thresholdMs", String.valueOf(SLOW_REQUEST_MILLIS))
                .register(registry);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestId = request.getHeader(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 12);
        }

        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_METHOD, request.getMethod());
        MDC.put(MDC_PATH, request.getRequestURI());
        response.setHeader(HEADER, requestId);

        long startedAt = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
            MDC.put(MDC_STATUS, String.valueOf(response.getStatus()));
            MDC.put(MDC_DURATION, String.valueOf(elapsedMillis));

            if (elapsedMillis >= SLOW_REQUEST_MILLIS) {
                slowRequests.increment();
                log.warn("Peticion lenta: {} {} -> {} en {} ms",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), elapsedMillis);
            }

            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_METHOD);
            MDC.remove(MDC_PATH);
            MDC.remove(MDC_STATUS);
            MDC.remove(MDC_DURATION);
        }
    }

    /**
     * Los recursos estaticos y la consola de desarrollo no aportan nada al
     * seguimiento y ensucian los logs.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/h2-console") || path.equals("/favicon.ico");
    }
}
