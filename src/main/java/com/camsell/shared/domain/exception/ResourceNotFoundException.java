package com.camsell.shared.domain.exception;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resource, Object identifier) {
        super("resource_not_found", "%s no encontrado: %s".formatted(resource, identifier));
    }

    public ResourceNotFoundException(String message) {
        super("resource_not_found", message);
    }
}
