package com.camsell.parameters.application;

import com.camsell.parameters.domain.model.ParameterGroup;
import com.camsell.parameters.domain.model.ParameterOption;
import com.camsell.parameters.domain.port.in.ParameterAdminUseCase;
import com.camsell.parameters.domain.port.out.ParameterRepositoryPort;
import com.camsell.shared.config.CacheConfig;
import com.camsell.shared.domain.exception.BusinessRuleException;
import com.camsell.shared.domain.exception.DuplicatedResourceException;
import com.camsell.shared.domain.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
@Caching(evict = {
        @CacheEvict(cacheNames = CacheConfig.PARAMETER_GROUPS, allEntries = true),
        @CacheEvict(cacheNames = CacheConfig.PARAMETER_OPTIONS, allEntries = true)
})
public class ParameterAdminService implements ParameterAdminUseCase {

    private final ParameterRepositoryPort repository;

    public ParameterAdminService(ParameterRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public ParameterGroup createGroup(CreateGroupCommand command) {
        String code = normalizeCode(command.code());
        if (repository.groupExists(code)) {
            throw new DuplicatedResourceException("Ya existe un grupo de parametros con codigo " + code);
        }
        ParameterGroup group = new ParameterGroup(
                null,
                code,
                command.name(),
                command.description(),
                command.dataType(),
                command.scope(),
                command.multiValued(),
                command.filterable(),
                command.requiredForListing(),
                command.showInSpecs(),
                command.sortOrder() == null ? 100 : command.sortOrder(),
                true,
                List.of());
        group.validate();
        return repository.saveGroup(group);
    }

    @Override
    public ParameterGroup updateGroup(String code, UpdateGroupCommand command) {
        ParameterGroup current = repository.findGroupByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de parametros", code));
        ParameterGroup updated = new ParameterGroup(
                current.id(),
                current.code(),
                command.name(),
                command.description(),
                command.dataType(),
                command.scope(),
                command.multiValued(),
                command.filterable(),
                command.requiredForListing(),
                command.showInSpecs(),
                command.sortOrder() == null ? current.sortOrder() : command.sortOrder(),
                command.active(),
                current.options());
        updated.validate();
        return repository.saveGroup(updated);
    }

    @Override
    public void deleteGroup(String code) {
        ParameterGroup group = repository.findGroupByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de parametros", code));
        if (!group.options().isEmpty()) {
            throw new BusinessRuleException(
                    "group_has_options",
                    "El grupo tiene %d opciones asociadas; eliminalas o desactiva el grupo"
                            .formatted(group.options().size()));
        }
        repository.deleteGroup(code);
    }

    @Override
    public ParameterOption createOption(String groupCode, SaveOptionCommand command) {
        requireGroup(groupCode);
        String code = normalizeCode(command.code());
        if (repository.findOption(groupCode, code).isPresent()) {
            throw new DuplicatedResourceException(
                    "La opcion %s ya existe en el grupo %s".formatted(code, groupCode));
        }
        return repository.saveOption(new ParameterOption(
                null,
                groupCode,
                code,
                command.label(),
                command.iconName(),
                command.colorHex(),
                command.sortOrder() == null ? 100 : command.sortOrder(),
                command.active()));
    }

    @Override
    public ParameterOption updateOption(String groupCode, Long optionId, SaveOptionCommand command) {
        ParameterOption current = requireOption(groupCode, optionId);
        return repository.saveOption(new ParameterOption(
                current.id(),
                groupCode,
                normalizeCode(command.code()),
                command.label(),
                command.iconName(),
                command.colorHex(),
                command.sortOrder() == null ? current.sortOrder() : command.sortOrder(),
                command.active()));
    }

    @Override
    public void deleteOption(String groupCode, Long optionId) {
        requireOption(groupCode, optionId);
        repository.deleteOption(optionId);
    }

    private void requireGroup(String groupCode) {
        if (!repository.groupExists(groupCode)) {
            throw new ResourceNotFoundException("Grupo de parametros", groupCode);
        }
    }

    private ParameterOption requireOption(String groupCode, Long optionId) {
        ParameterOption option = repository.findOptionById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Opcion de parametro", optionId));
        if (!option.groupCode().equals(groupCode)) {
            throw new ResourceNotFoundException(
                    "La opcion %d no pertenece al grupo %s".formatted(optionId, groupCode));
        }
        return option;
    }

    private String normalizeCode(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessRuleException("El codigo es obligatorio");
        }
        return raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }
}
