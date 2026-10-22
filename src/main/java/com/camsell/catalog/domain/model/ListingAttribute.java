package com.camsell.catalog.domain.model;

import java.math.BigDecimal;

/**
 * Valor de un parametro aplicado a una publicacion. Es el mecanismo que permite
 * agregar caracteristicas nuevas al vehiculo (traccion, techo solar, garantia,
 * etc.) sin alterar el esquema de la tabla de publicaciones.
 */
public record ListingAttribute(
        Long id,
        String groupCode,
        String optionCode,
        String textValue,
        BigDecimal numberValue,
        Boolean boolValue) {

    public static ListingAttribute option(String groupCode, String optionCode) {
        return new ListingAttribute(null, groupCode, optionCode, null, null, null);
    }

    public static ListingAttribute text(String groupCode, String value) {
        return new ListingAttribute(null, groupCode, null, value, null, null);
    }

    public static ListingAttribute number(String groupCode, BigDecimal value) {
        return new ListingAttribute(null, groupCode, null, null, value, null);
    }

    public static ListingAttribute flag(String groupCode, Boolean value) {
        return new ListingAttribute(null, groupCode, null, null, null, value);
    }

    public boolean hasValue() {
        return optionCode != null || textValue != null || numberValue != null || boolValue != null;
    }

    /** Representacion legible usada en la ficha tecnica del vehiculo. */
    public String displayValue() {
        if (optionCode != null) {
            return optionCode;
        }
        if (textValue != null) {
            return textValue;
        }
        if (numberValue != null) {
            return numberValue.stripTrailingZeros().toPlainString();
        }
        if (boolValue != null) {
            return boolValue ? "Si" : "No";
        }
        return "";
    }
}
