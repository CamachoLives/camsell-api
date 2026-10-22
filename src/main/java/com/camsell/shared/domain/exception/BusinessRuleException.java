package com.camsell.shared.domain.exception;

public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super("business_rule_violation", message);
    }

    public BusinessRuleException(String code, String message) {
        super(code, message);
    }
}
