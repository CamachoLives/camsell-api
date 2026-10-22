package com.camsell.parameters.infrastructure.persistence;

import com.camsell.parameters.domain.model.ParameterGroup;
import com.camsell.parameters.domain.model.ParameterOption;
import com.camsell.parameters.domain.port.out.ParameterRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Adaptador JPA del catalogo de parametros. */
@Repository
public class ParameterPersistenceAdapter implements ParameterRepositoryPort {

    private final ParameterGroupJpaRepository groups;
    private final ParameterOptionJpaRepository options;

    public ParameterPersistenceAdapter(ParameterGroupJpaRepository groups, ParameterOptionJpaRepository options) {
        this.groups = groups;
        this.options = options;
    }

    @Override
    public List<ParameterGroup> findAllGroups(boolean includeInactive) {
        List<ParameterGroupEntity> groupEntities = includeInactive
                ? groups.findAllByOrderBySortOrderAscNameAsc()
                : groups.findAllByActiveTrueOrderBySortOrderAscNameAsc();
        List<ParameterOptionEntity> optionEntities = includeInactive
                ? options.findAllByOrderByGroupCodeAscSortOrderAscLabelAsc()
                : options.findAllByActiveTrueOrderByGroupCodeAscSortOrderAscLabelAsc();

        Map<String, List<ParameterOption>> byGroup = optionEntities.stream()
                .map(ParameterMapper::toDomain)
                .collect(Collectors.groupingBy(ParameterOption::groupCode));

        List<ParameterGroup> result = new ArrayList<>(groupEntities.size());
        for (ParameterGroupEntity entity : groupEntities) {
            result.add(ParameterMapper.toDomain(entity, byGroup.getOrDefault(entity.getCode(), List.of())));
        }
        return result;
    }

    @Override
    public Optional<ParameterGroup> findGroupByCode(String code) {
        return groups.findByCode(code)
                .map(entity -> ParameterMapper.toDomain(
                        entity,
                        options.findByGroupCodeOrderBySortOrderAscLabelAsc(code).stream()
                                .map(ParameterMapper::toDomain)
                                .toList()));
    }

    @Override
    public boolean groupExists(String code) {
        return groups.existsByCode(code);
    }

    @Override
    public ParameterGroup saveGroup(ParameterGroup group) {
        ParameterGroupEntity entity = group.id() == null
                ? new ParameterGroupEntity()
                : groups.findById(group.id()).orElseGet(ParameterGroupEntity::new);
        ParameterMapper.applyToEntity(group, entity);
        ParameterGroupEntity saved = groups.save(entity);
        return ParameterMapper.toDomain(saved, group.options());
    }

    @Override
    public void deleteGroup(String code) {
        options.deleteByGroupCode(code);
        groups.deleteByCode(code);
    }

    @Override
    public List<ParameterOption> findOptionsByGroup(String groupCode, boolean includeInactive) {
        List<ParameterOptionEntity> entities = includeInactive
                ? options.findByGroupCodeOrderBySortOrderAscLabelAsc(groupCode)
                : options.findByGroupCodeAndActiveTrueOrderBySortOrderAscLabelAsc(groupCode);
        return entities.stream().map(ParameterMapper::toDomain).toList();
    }

    @Override
    public Optional<ParameterOption> findOption(String groupCode, String optionCode) {
        return options.findByGroupCodeAndCode(groupCode, optionCode).map(ParameterMapper::toDomain);
    }

    @Override
    public Optional<ParameterOption> findOptionById(Long id) {
        return options.findById(id).map(ParameterMapper::toDomain);
    }

    @Override
    public ParameterOption saveOption(ParameterOption option) {
        ParameterOptionEntity entity = option.id() == null
                ? new ParameterOptionEntity()
                : options.findById(option.id()).orElseGet(ParameterOptionEntity::new);
        ParameterMapper.applyToEntity(option, entity);
        return ParameterMapper.toDomain(options.save(entity));
    }

    @Override
    public void deleteOption(Long id) {
        options.deleteById(id);
    }
}
