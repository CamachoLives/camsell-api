package com.camsell.parameters.domain.port.out;

import com.camsell.parameters.domain.model.ParameterGroup;
import com.camsell.parameters.domain.model.ParameterOption;

import java.util.List;
import java.util.Optional;

/** Puerto de salida del catalogo de parametros. */
public interface ParameterRepositoryPort {

    List<ParameterGroup> findAllGroups(boolean includeInactive);

    Optional<ParameterGroup> findGroupByCode(String code);

    boolean groupExists(String code);

    ParameterGroup saveGroup(ParameterGroup group);

    void deleteGroup(String code);

    List<ParameterOption> findOptionsByGroup(String groupCode, boolean includeInactive);

    Optional<ParameterOption> findOption(String groupCode, String optionCode);

    Optional<ParameterOption> findOptionById(Long id);

    ParameterOption saveOption(ParameterOption option);

    void deleteOption(Long id);
}
