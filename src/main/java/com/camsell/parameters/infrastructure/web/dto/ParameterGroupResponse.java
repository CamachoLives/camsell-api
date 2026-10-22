package com.camsell.parameters.infrastructure.web.dto;

import com.camsell.parameters.domain.model.ParameterDataType;
import com.camsell.parameters.domain.model.ParameterGroup;
import com.camsell.parameters.domain.model.ParameterOption;
import com.camsell.parameters.domain.model.ParameterScope;

import java.util.List;

/**
 * Contrato publico de un grupo de parametros. El frontend lo interpreta para
 * generar filtros y campos de formulario de forma dinamica.
 */
public record ParameterGroupResponse(
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
        List<Option> options) {

    public record Option(
            Long id,
            String code,
            String label,
            String iconName,
            String colorHex,
            int sortOrder,
            boolean active) {

        public static Option from(ParameterOption option) {
            return new Option(
                    option.id(),
                    option.code(),
                    option.label(),
                    option.iconName(),
                    option.colorHex(),
                    option.sortOrder(),
                    option.active());
        }
    }

    public static ParameterGroupResponse from(ParameterGroup group) {
        return new ParameterGroupResponse(
                group.id(),
                group.code(),
                group.name(),
                group.description(),
                group.dataType(),
                group.scope(),
                group.multiValued(),
                group.filterable(),
                group.requiredForListing(),
                group.showInSpecs(),
                group.sortOrder(),
                group.active(),
                group.options().stream().map(Option::from).toList());
    }
}
