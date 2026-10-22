package com.camsell.catalog.domain.model;

import com.camsell.shared.domain.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Valor monetario con moneda explicita. */
public record Money(BigDecimal amount, String currency) {

    public static final String DEFAULT_CURRENCY = "COP";

    public Money {
        if (amount == null) {
            throw new BusinessRuleException("El precio es obligatorio");
        }
        if (amount.signum() <= 0) {
            throw new BusinessRuleException("El precio debe ser mayor que cero");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        currency = currency == null || currency.isBlank() ? DEFAULT_CURRENCY : currency.toUpperCase();
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public boolean isLowerThan(Money other) {
        return amount.compareTo(other.amount) < 0;
    }

    /** Porcentaje que representa este valor respecto a otro, para mostrar descuentos. */
    public BigDecimal percentageOf(Money reference) {
        return amount.multiply(BigDecimal.valueOf(100))
                .divide(reference.amount, 2, RoundingMode.HALF_UP);
    }
}
