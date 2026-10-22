package com.camsell.parameters.infrastructure.web;

import com.camsell.parameters.domain.port.in.ParameterAdminUseCase;
import com.camsell.parameters.domain.port.in.ParameterQueryUseCase;
import com.camsell.parameters.infrastructure.web.dto.ParameterGroupResponse;
import com.camsell.parameters.infrastructure.web.dto.ParameterPayloads;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consola de administracion del catalogo de parametros. */
@RestController
@RequestMapping("/api/v1/admin/parameters")
@PreAuthorize("hasRole('ADMIN')")
public class ParameterAdminController {

    private final ParameterAdminUseCase admin;
    private final ParameterQueryUseCase query;

    public ParameterAdminController(ParameterAdminUseCase admin, ParameterQueryUseCase query) {
        this.admin = admin;
        this.query = query;
    }

    @GetMapping("/groups")
    public List<ParameterGroupResponse> groups() {
        return query.listGroups(true).stream().map(ParameterGroupResponse::from).toList();
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public ParameterGroupResponse createGroup(@Valid @RequestBody ParameterPayloads.GroupPayload payload) {
        return ParameterGroupResponse.from(admin.createGroup(new ParameterAdminUseCase.CreateGroupCommand(
                payload.code(),
                payload.name(),
                payload.description(),
                payload.dataType(),
                payload.scopeOrDefault(),
                payload.multiValued(),
                payload.filterable(),
                payload.requiredForListing(),
                payload.showInSpecs(),
                payload.sortOrder())));
    }

    @PutMapping("/groups/{code}")
    public ParameterGroupResponse updateGroup(
            @PathVariable String code,
            @Valid @RequestBody ParameterPayloads.GroupPayload payload) {
        return ParameterGroupResponse.from(admin.updateGroup(code, new ParameterAdminUseCase.UpdateGroupCommand(
                payload.name(),
                payload.description(),
                payload.dataType(),
                payload.scopeOrDefault(),
                payload.multiValued(),
                payload.filterable(),
                payload.requiredForListing(),
                payload.showInSpecs(),
                payload.sortOrder(),
                payload.activeOrDefault())));
    }

    @DeleteMapping("/groups/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable String code) {
        admin.deleteGroup(code);
    }

    @PostMapping("/groups/{code}/options")
    @ResponseStatus(HttpStatus.CREATED)
    public ParameterGroupResponse.Option createOption(
            @PathVariable String code,
            @Valid @RequestBody ParameterPayloads.OptionPayload payload) {
        return ParameterGroupResponse.Option.from(admin.createOption(code, toCommand(payload)));
    }

    @PutMapping("/groups/{code}/options/{optionId}")
    public ParameterGroupResponse.Option updateOption(
            @PathVariable String code,
            @PathVariable Long optionId,
            @Valid @RequestBody ParameterPayloads.OptionPayload payload) {
        return ParameterGroupResponse.Option.from(admin.updateOption(code, optionId, toCommand(payload)));
    }

    @DeleteMapping("/groups/{code}/options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOption(@PathVariable String code, @PathVariable Long optionId) {
        admin.deleteOption(code, optionId);
    }

    private ParameterAdminUseCase.SaveOptionCommand toCommand(ParameterPayloads.OptionPayload payload) {
        return new ParameterAdminUseCase.SaveOptionCommand(
                payload.code(),
                payload.label(),
                payload.iconName(),
                payload.colorHex(),
                payload.sortOrder(),
                payload.activeOrDefault());
    }
}
