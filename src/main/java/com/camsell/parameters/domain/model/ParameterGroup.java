package com.camsell.parameters.domain.model;

import com.camsell.shared.domain.exception.BusinessRuleException;

import java.util.List;

/**
 * Grupo de parametros del sistema. Es la pieza que hace parametrizable el
 * marketplace: agregar un grupo nuevo agrega automaticamente un campo al
 * formulario de publicacion y, si es filtrable, un filtro en la busqueda, sin
 * cambiar el esquema de la base de datos ni recompilar el frontend.
 */
public record ParameterGroup(
        Long id,
        String code,
        String name,
        String description,
        ParameterDataType dataType,
        ParameterScope scope,
        boolean multiValued,
        boolean filterable,
        boolean requiredForListing,
        boolean showInSpecs,
        int sortOrder,
        boolean active,
        List<ParameterOption> options) {

    public ParameterGroup {
        options = options == null ? List.of() : List.copyOf(options);
        scope = scope == null ? ParameterScope.LISTING : scope;
    }

    /** Valida la coherencia del grupo antes de persistirlo. */
    public void validate() {
        if (code == null || code.isBlank()) {
            throw new BusinessRuleException("El codigo del grupo es obligatorio");
        }
        if (!code.matches("[A-Z][A-Z0-9_]{1,39}")) {
            throw new BusinessRuleException(
                    "invalid_group_code",
                    "El codigo del grupo debe ser MAYUSCULAS_CON_GUION_BAJO: " + code);
        }
        if (multiValued && dataType != ParameterDataType.OPTION) {
            throw new BusinessRuleException(
                    "multi_value_requires_options",
                    "Solo los grupos de tipo OPTION pueden aceptar multiples valores");
        }
        if (requiredForListing && !scope.appliesToListings()) {
            throw new BusinessRuleException(
                    "required_outside_listing_scope",
                    "Un grupo obligatorio para publicar debe tener ambito LISTING o GENERAL");
        }
    }

    public ParameterGroup withOptions(List<ParameterOption> newOptions) {
        return new ParameterGroup(id, code, name, description, dataType, scope, multiValued, filterable,
                requiredForListing, showInSpecs, sortOrder, active, newOptions);
    }

    public List<ParameterOption> activeOptions() {
        return options.stream().filter(ParameterOption::active).toList();
    }

    public boolean appliesToListings() {
        return scope.appliesToListings();
    }
}
