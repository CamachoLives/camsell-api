package com.camsell.shared.domain.exception;

public class OperationNotAllowedException extends DomainException {

    public OperationNotAllowedException(String message) {
        super("operation_not_allowed", message);
    }
}
