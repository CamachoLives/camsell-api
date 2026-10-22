package com.camsell.shared.domain.exception;

public class DuplicatedResourceException extends DomainException {

    public DuplicatedResourceException(String message) {
        super("duplicated_resource", message);
    }
}
