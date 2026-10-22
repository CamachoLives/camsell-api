package com.camsell.parameters.domain.port.in;

import com.camsell.parameters.domain.model.ParameterDataType;
import com.camsell.parameters.domain.model.ParameterGroup;
import com.camsell.parameters.domain.model.ParameterOption;
import com.camsell.parameters.domain.model.ParameterScope;

/** Administracion del catalogo de parametros (rol ADMIN). */
public interface ParameterAdminUseCase {

    ParameterGroup createGroup(CreateGroupCommand command);

    ParameterGroup updateGroup(String code, UpdateGroupCommand command);

    void deleteGroup(String code);

    ParameterOption createOption(String groupCode, SaveOptionCommand command);

    ParameterOption updateOption(String groupCode, Long optionId, SaveOptionCommand command);

    void deleteOption(String groupCode, Long optionId);

    record CreateGroupCommand(
            String code,
            String name,
            String description,
            ParameterDataType dataType,
            ParameterScope scope,
            boolean multiValued,
            boolean filterable,
            boolean requiredForListing,
            boolean showInSpecs,
            Integer sortOrder) {
    }

    record UpdateGroupCommand(
            String name,
            String description,
            ParameterDataType dataType,
            ParameterScope scope,
            boolean multiValued,
            boolean filterable,
            boolean requiredForListing,
            boolean showInSpecs,
            Integer sortOrder,
            boolean active) {
    }

    record SaveOptionCommand(
            String code,
            String label,
            String iconName,
            String colorHex,
            Integer sortOrder,
            boolean active) {
    }
}
