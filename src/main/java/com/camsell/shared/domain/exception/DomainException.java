package com.camsell.shared.domain.exception;

/**
 * Raiz de todas las excepciones de negocio. Cada excepcion expone un codigo
 * estable que la capa de API traduce a un {@code ProblemDetail} RFC-9457.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
