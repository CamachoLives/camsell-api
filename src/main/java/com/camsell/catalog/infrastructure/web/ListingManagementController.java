package com.camsell.catalog.infrastructure.web;

import com.camsell.catalog.domain.model.Listing;
import com.camsell.catalog.domain.model.ListingSearchQuery;
import com.camsell.catalog.domain.port.in.ListingCommandUseCase;
import com.camsell.catalog.domain.port.in.ListingQueryUseCase;
import com.camsell.catalog.infrastructure.web.dto.ListingPayloads;
import com.camsell.catalog.infrastructure.web.dto.ListingResponses;
import com.camsell.shared.api.dto.PageResponse;
import com.camsell.shared.domain.page.PageQuery;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Publicacion y gestion de vehiculos del usuario autenticado. */
@RestController
@RequestMapping("/api/v1/listings")
public class ListingManagementController {

    private final ListingCommandUseCase commands;
    private final ListingQueryUseCase queries;

    public ListingManagementController(ListingCommandUseCase commands, ListingQueryUseCase queries) {
        this.commands = commands;
        this.queries = queries;
    }

    @GetMapping("/mine")
    public PageResponse<ListingResponses.Summary> mine(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {
        ListingSearchQuery query = new ListingSearchQuery(
                text, null, null, null, null, null, null, null, null,
                null, null, null, null, null);
        return PageResponse.from(queries
                .listOwnedByCurrentUser(query, PageQuery.of(page, size, sort == null ? "createdAt" : sort, direction))
                .map(ListingResponses.Summary::from));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListingResponses.Detail create(@Valid @RequestBody ListingPayloads.SaveListing payload) {
        Listing created = commands.create(toCommand(payload));
        return ListingResponses.Detail.from(created, false, 0);
    }

    @PutMapping("/{id}")
    public ListingResponses.Detail update(
            @PathVariable Long id,
            @Valid @RequestBody ListingPayloads.SaveListing payload) {
        return ListingResponses.Detail.from(commands.update(id, toCommand(payload)), false, 0);
    }

    @PatchMapping("/{id}/status")
    public ListingResponses.Detail changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ListingPayloads.ChangeStatus payload) {
        return ListingResponses.Detail.from(commands.changeStatus(id, payload.status()), false, 0);
    }

    @PutMapping("/{id}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ListingResponses.Detail setFeatured(@PathVariable Long id, @RequestParam boolean featured) {
        return ListingResponses.Detail.from(commands.setFeatured(id, featured), false, 0);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        commands.delete(id);
    }

    private ListingCommandUseCase.SaveListingCommand toCommand(ListingPayloads.SaveListing payload) {
        List<ListingCommandUseCase.ImageCommand> images = payload.images() == null
                ? List.of()
                : payload.images().stream()
                        .map(image -> new ListingCommandUseCase.ImageCommand(
                                image.url(), image.alt(), image.position()))
                        .toList();
        List<ListingCommandUseCase.AttributeCommand> attributes = payload.attributes() == null
                ? List.of()
                : payload.attributes().stream()
                        .map(attribute -> new ListingCommandUseCase.AttributeCommand(
                                attribute.groupCode(),
                                attribute.optionCode(),
                                attribute.textValue(),
                                attribute.numberValue(),
                                attribute.boolValue()))
                        .toList();

        return new ListingCommandUseCase.SaveListingCommand(
                payload.brandId(),
                payload.modelId(),
                payload.title(),
                payload.year(),
                payload.mileageKm(),
                payload.price(),
                payload.currency(),
                payload.negotiable(),
                payload.description(),
                payload.cityCode(),
                payload.publishNow(),
                images,
                attributes);
    }
}
