package com.camsell.parameters.infrastructure.persistence;

import com.camsell.parameters.domain.model.ParameterGroup;
import com.camsell.parameters.domain.model.ParameterOption;

import java.util.List;

/** Traduce entre entidades JPA y el modelo de dominio de parametros. */
final class ParameterMapper {

    private ParameterMapper() {
    }

    static ParameterGroup toDomain(ParameterGroupEntity entity, List<ParameterOption> options) {
        return new ParameterGroup(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getDataType(),
                entity.getScope(),
                entity.isMultiValued(),
                entity.isFilterable(),
                entity.isRequiredForListing(),
                entity.isShowInSpecs(),
                entity.getSortOrder(),
                entity.isActive(),
                options);
    }

    static ParameterOption toDomain(ParameterOptionEntity entity) {
        return new ParameterOption(
                entity.getId(),
                entity.getGroupCode(),
                entity.getCode(),
                entity.getLabel(),
                entity.getIconName(),
                entity.getColorHex(),
                entity.getSortOrder(),
                entity.isActive());
    }

    static void applyToEntity(ParameterGroup group, ParameterGroupEntity entity) {
        entity.setCode(group.code());
        entity.setName(group.name());
        entity.setDescription(group.description());
        entity.setDataType(group.dataType());
        entity.setScope(group.scope());
        entity.setMultiValued(group.multiValued());
        entity.setFilterable(group.filterable());
        entity.setRequiredForListing(group.requiredForListing());
        entity.setShowInSpecs(group.showInSpecs());
        entity.setSortOrder(group.sortOrder());
        entity.setActive(group.active());
    }

    static void applyToEntity(ParameterOption option, ParameterOptionEntity entity) {
        entity.setGroupCode(option.groupCode());
        entity.setCode(option.code());
        entity.setLabel(option.label());
        entity.setIconName(option.iconName());
        entity.setColorHex(option.colorHex());
        entity.setSortOrder(option.sortOrder());
        entity.setActive(option.active());
    }
}
