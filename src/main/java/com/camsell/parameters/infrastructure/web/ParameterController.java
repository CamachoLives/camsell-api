package com.camsell.parameters.infrastructure.web;

import com.camsell.parameters.domain.port.in.ParameterQueryUseCase;
import com.camsell.parameters.infrastructure.web.dto.ParameterGroupResponse;
import com.camsell.shared.api.CachePolicy;
import com.camsell.shared.api.ResourceVersions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

/**
 * Esquema de parametros expuesto al cliente. Es la unica fuente de verdad para
 * los filtros de busqueda y los campos del formulario de publicacion.
 *
 * <p>El cliente lo pide en cada pantalla y solo cambia cuando un administrador
 * lo edita, asi que se sirve con cache publica y se revalida por ETag. Cuando
 * el ETag coincide se responde 304 sin consultar la base de datos ni serializar
 * la respuesta.
 */
@RestController
@RequestMapping("/api/v1/parameters")
public class ParameterController {

    private final ParameterQueryUseCase parameters;
    private final ResourceVersions versions;

    public ParameterController(ParameterQueryUseCase parameters, ResourceVersions versions) {
        this.parameters = parameters;
        this.versions = versions;
    }

    @GetMapping("/groups")
    public ResponseEntity<List<ParameterGroupResponse>> groups(
            @RequestParam(defaultValue = "false") boolean includeInactive,
            WebRequest request) {
        String etag = versions.etagOf(ResourceVersions.Family.PARAMETERS);
        if (request.checkNotModified(etag)) {
            return null;
        }
        List<ParameterGroupResponse> body = parameters.listGroups(includeInactive).stream()
                .map(ParameterGroupResponse::from)
                .toList();
        return CachePolicy.validated(body, CachePolicy.CONFIGURATION, etag);
    }

    @GetMapping("/groups/{code}")
    public ResponseEntity<ParameterGroupResponse> group(@PathVariable String code, WebRequest request) {
        String etag = versions.etagOf(ResourceVersions.Family.PARAMETERS);
        if (request.checkNotModified(etag)) {
            return null;
        }
        return CachePolicy.validated(
                ParameterGroupResponse.from(parameters.getGroup(code)),
                CachePolicy.CONFIGURATION,
                etag);
    }

    @GetMapping("/groups/{code}/options")
    public ResponseEntity<List<ParameterGroupResponse.Option>> options(
            @PathVariable String code,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            WebRequest request) {
        String etag = versions.etagOf(ResourceVersions.Family.PARAMETERS);
        if (request.checkNotModified(etag)) {
            return null;
        }
        List<ParameterGroupResponse.Option> body = parameters.listOptions(code, includeInactive)
                .stream()
                .map(ParameterGroupResponse.Option::from)
                .toList();
        return CachePolicy.validated(body, CachePolicy.CONFIGURATION, etag);
    }
}
