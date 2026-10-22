package com.camsell.parameters.domain.model;

/**
 * Valor administrable de un grupo de parametros (por ejemplo la opcion
 * {@code DIESEL} del grupo {@code FUEL_TYPE}).
 */
public record ParameterOption(
        Long id,
        String groupCode,
        String code,
        String label,
        String iconName,
        String colorHex,
        int sortOrder,
        boolean active) {

    public ParameterOption withGroup(String newGroupCode) {
        return new ParameterOption(id, newGroupCode, code, label, iconName, colorHex, sortOrder, active);
    }
}
