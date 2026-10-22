package com.camsell.parameters.application;

import com.camsell.parameters.domain.model.ParameterGroup;
import com.camsell.parameters.domain.model.ParameterOption;
import com.camsell.parameters.domain.port.in.ParameterQueryUseCase;
import com.camsell.parameters.domain.port.out.ParameterRepositoryPort;
import com.camsell.shared.config.CacheConfig;
import com.camsell.shared.domain.exception.BusinessRuleException;
import com.camsell.shared.domain.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ParameterQueryService implements ParameterQueryUseCase {

    private final ParameterRepositoryPort repository;

    public ParameterQueryService(ParameterRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.PARAMETER_GROUPS, key = "#includeInactive")
    public List<ParameterGroup> listGroups(boolean includeInactive) {
        return repository.findAllGroups(includeInactive);
    }

    @Override
    public ParameterGroup getGroup(String code) {
        return repository.findGroupByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de parametros", code));
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.PARAMETER_OPTIONS, key = "#groupCode + ':' + #includeInactive")
    public List<ParameterOption> listOptions(String groupCode, boolean includeInactive) {
        if (!repository.groupExists(groupCode)) {
            throw new ResourceNotFoundException("Grupo de parametros", groupCode);
        }
        return repository.findOptionsByGroup(groupCode, includeInactive);
    }

    @Override
    public void requireValidOption(String groupCode, String optionCode) {
        ParameterOption option = repository.findOption(groupCode, optionCode)
                .orElseThrow(() -> new BusinessRuleException(
                        "invalid_parameter_option",
                        "La opcion '%s' no existe en el grupo '%s'".formatted(optionCode, groupCode)));
        if (!option.active()) {
            throw new BusinessRuleException(
                    "inactive_parameter_option",
                    "La opcion '%s' del grupo '%s' esta inactiva".formatted(optionCode, groupCode));
        }
    }
}
