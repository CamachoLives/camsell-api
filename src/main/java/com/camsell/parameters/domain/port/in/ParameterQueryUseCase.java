package com.camsell.parameters.domain.port.in;

import com.camsell.parameters.domain.model.ParameterGroup;
import com.camsell.parameters.domain.model.ParameterOption;

import java.util.List;

/** Lectura del catalogo de parametros. */
public interface ParameterQueryUseCase {

    /**
     * Devuelve el esquema completo de parametros. El frontend lo usa para
     * construir filtros y formularios en tiempo de ejecucion.
     */
    List<ParameterGroup> listGroups(boolean includeInactive);

    ParameterGroup getGroup(String code);

    List<ParameterOption> listOptions(String groupCode, boolean includeInactive);

    /** Valida que un codigo de opcion exista y este activo en su grupo. */
    void requireValidOption(String groupCode, String optionCode);
}
