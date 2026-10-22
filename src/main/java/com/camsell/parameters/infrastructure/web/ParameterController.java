package com.camsell.parameters.infrastructure.web;

import com.camsell.parameters.domain.port.in.ParameterQueryUseCase;
import com.camsell.parameters.infrastructure.web.dto.ParameterGroupResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Esquema de parametros expuesto al cliente. Es la unica fuente de verdad para
 * los filtros de busqueda y los campos del formulario de publicacion.
 */
@RestController
@RequestMapping("/api/v1/parameters")
public class ParameterController {

    private final ParameterQueryUseCase parameters;

    public ParameterController(ParameterQueryUseCase parameters) {
        this.parameters = parameters;
    }

    @GetMapping("/groups")
    public List<ParameterGroupResponse> groups(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return parameters.listGroups(includeInactive).stream()
                .map(ParameterGroupResponse::from)
                .toList();
    }

    @GetMapping("/groups/{code}")
    public ParameterGroupResponse group(@PathVariable String code) {
        return ParameterGroupResponse.from(parameters.getGroup(code));
    }

    @GetMapping("/groups/{code}/options")
    public List<ParameterGroupResponse.Option> options(
            @PathVariable String code,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return parameters.listOptions(code, includeInactive).stream()
                .map(ParameterGroupResponse.Option::from)
                .toList();
    }
}
