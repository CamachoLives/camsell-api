package com.camsell.catalog.application;

import com.camsell.catalog.domain.model.Listing;
import com.camsell.catalog.domain.model.ListingAttribute;
import com.camsell.catalog.domain.model.ListingImage;
import com.camsell.catalog.domain.model.ListingStatus;
import com.camsell.catalog.domain.model.Money;
import com.camsell.catalog.domain.port.in.ListingCommandUseCase;
import com.camsell.catalog.domain.port.out.ListingRepositoryPort;
import com.camsell.catalog.domain.port.out.SellerLookupPort;
import com.camsell.identity.domain.port.in.CurrentUserPort;
import com.camsell.parameters.domain.model.ParameterGroup;
import com.camsell.parameters.domain.model.VehicleModel;
import com.camsell.parameters.domain.port.in.BrandUseCase;
import com.camsell.parameters.domain.port.in.ParameterQueryUseCase;
import com.camsell.shared.config.CamsellProperties;
import com.camsell.shared.domain.exception.BusinessRuleException;
import com.camsell.shared.domain.exception.OperationNotAllowedException;
import com.camsell.shared.domain.exception.ResourceNotFoundException;
import com.camsell.shared.domain.util.Slugs;
import io.micrometer.core.annotation.Counted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Escritura del catalogo. Toda publicacion se valida contra el catalogo de
 * parametros vigente, por lo que las reglas cambian con la configuracion y no
 * con el codigo.
 */
@Service
@Transactional
public class ListingCommandService implements ListingCommandUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListingCommandService.class);
    private static final String REFERENCE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ListingRepositoryPort listings;
    private final ParameterQueryUseCase parameters;
    private final BrandUseCase brands;
    private final CurrentUserPort currentUser;
    private final SellerLookupPort sellers;
    private final CamsellProperties properties;

    public ListingCommandService(
            ListingRepositoryPort listings,
            ParameterQueryUseCase parameters,
            BrandUseCase brands,
            CurrentUserPort currentUser,
            SellerLookupPort sellers,
            CamsellProperties properties) {
        this.listings = listings;
        this.parameters = parameters;
        this.brands = brands;
        this.currentUser = currentUser;
        this.sellers = sellers;
        this.properties = properties;
    }

    @Override
    @Counted(value = "camsell.listings.created", description = "Publicaciones creadas")
    public Listing create(SaveListingCommand command) {
        Long sellerId = currentUser.requireCurrentUserId();
        SellerLookupPort.SellerInfo seller = sellers.findSeller(sellerId);
        VehicleModel model = resolveModel(command);

        List<ListingAttribute> attributes = validateAndBuildAttributes(command.attributes(), command.publishNow());
        List<ListingImage> images = buildImages(command.images());
        String title = resolveTitle(command, model);

        Listing draft = new Listing(
                null,
                generateReference(),
                generateSlug(model, command.year(), title),
                sellerId,
                seller.fullName(),
                model.brandId(),
                model.brandName(),
                model.id(),
                model.name(),
                title,
                command.year(),
                command.mileageKm(),
                new Money(command.price(), command.currency()),
                command.negotiable(),
                command.description(),
                command.cityCode(),
                ListingStatus.DRAFT,
                false,
                0L,
                null,
                null,
                images,
                attributes);

        Listing toPersist = draft;
        if (command.publishNow()) {
            draft.validateForPublication(maxImages(), minYear());
            toPersist = draft.withStatus(ListingStatus.PUBLISHED, Instant.now());
        }
        Listing saved = listings.save(toPersist);
        log.info("Publicacion creada ref={} estado={} vendedor={}", saved.reference(), saved.status(), sellerId);
        return saved;
    }

    @Override
    public Listing update(Long id, SaveListingCommand command) {
        Listing current = requireManageable(id);
        VehicleModel model = resolveModel(command);
        boolean willBeVisible = command.publishNow() || current.status().isPubliclyVisible();

        List<ListingAttribute> attributes = validateAndBuildAttributes(command.attributes(), willBeVisible);
        List<ListingImage> images = buildImages(command.images());
        String title = resolveTitle(command, model);

        Listing updated = new Listing(
                current.id(),
                current.reference(),
                current.slug(),
                current.sellerId(),
                current.sellerName(),
                model.brandId(),
                model.brandName(),
                model.id(),
                model.name(),
                title,
                command.year(),
                command.mileageKm(),
                new Money(command.price(), command.currency()),
                command.negotiable(),
                command.description(),
                command.cityCode(),
                current.status(),
                current.featured(),
                current.viewCount(),
                current.publishedAt(),
                current.createdAt(),
                images,
                attributes);

        if (willBeVisible) {
            updated.validateForPublication(maxImages(), minYear());
            if (current.status() == ListingStatus.DRAFT) {
                updated = updated.withStatus(ListingStatus.PUBLISHED, Instant.now());
            }
        }
        return listings.save(updated);
    }

    @Override
    @Counted(value = "camsell.listings.status_changed", description = "Cambios de estado de publicaciones")
    public Listing changeStatus(Long id, ListingStatus status) {
        Listing current = requireManageable(id);
        if (status.isPubliclyVisible()) {
            current.validateForPublication(maxImages(), minYear());
        }
        Listing updated = current.withStatus(status, Instant.now());
        log.info("Publicacion ref={} paso de {} a {}", current.reference(), current.status(), status);
        return listings.save(updated);
    }

    @Override
    public Listing setFeatured(Long id, boolean featured) {
        if (!currentUser.isAdmin()) {
            throw new OperationNotAllowedException("Solo un administrador puede destacar publicaciones");
        }
        Listing current = listings.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicacion", id));
        return listings.save(current.withFeatured(featured));
    }

    @Override
    public void delete(Long id) {
        requireManageable(id);
        listings.deleteById(id);
    }

    private Listing requireManageable(Long id) {
        Listing listing = listings.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicacion", id));
        if (!currentUser.canManage(listing.sellerId())) {
            throw new OperationNotAllowedException("Solo el vendedor o un administrador pueden modificar esta publicacion");
        }
        return listing;
    }

    private VehicleModel resolveModel(SaveListingCommand command) {
        if (command.modelId() == null) {
            throw new BusinessRuleException("Debes seleccionar el modelo del vehiculo");
        }
        VehicleModel model = brands.getModel(command.modelId());
        if (command.brandId() != null && !command.brandId().equals(model.brandId())) {
            throw new BusinessRuleException(
                    "model_brand_mismatch",
                    "El modelo seleccionado no pertenece a la marca indicada");
        }
        if (!model.active()) {
            throw new BusinessRuleException("El modelo seleccionado esta inactivo");
        }
        return model;
    }

    /**
     * Valida los valores de parametros contra el esquema vigente: existencia del
     * grupo, tipo de dato, cardinalidad y obligatoriedad.
     */
    private List<ListingAttribute> validateAndBuildAttributes(List<AttributeCommand> commands, boolean publishing) {
        List<AttributeCommand> incoming = commands == null ? List.of() : commands;
        // Solo los grupos con ambito de catalogo participan en una publicacion.
        Map<String, ParameterGroup> schema = parameters.listGroups(false).stream()
                .filter(ParameterGroup::appliesToListings)
                .collect(Collectors.toMap(ParameterGroup::code, group -> group));

        Map<String, Integer> occurrences = new HashMap<>();
        List<ListingAttribute> attributes = new ArrayList<>();

        for (AttributeCommand attribute : incoming) {
            ParameterGroup group = schema.get(attribute.groupCode());
            if (group == null) {
                throw new BusinessRuleException(
                        "unknown_parameter_group",
                        "El grupo de parametros '%s' no existe o esta inactivo".formatted(attribute.groupCode()));
            }
            ListingAttribute built = buildAttribute(group, attribute);
            if (!built.hasValue()) {
                continue;
            }
            int count = occurrences.merge(group.code(), 1, Integer::sum);
            if (count > 1 && !group.multiValued()) {
                throw new BusinessRuleException(
                        "single_value_group",
                        "El grupo '%s' admite un unico valor".formatted(group.code()));
            }
            attributes.add(built);
        }

        if (publishing) {
            Set<String> provided = occurrences.keySet();
            List<String> missing = schema.values().stream()
                    .filter(ParameterGroup::requiredForListing)
                    .map(ParameterGroup::code)
                    .filter(code -> !provided.contains(code))
                    .toList();
            if (!missing.isEmpty()) {
                throw new BusinessRuleException(
                        "missing_required_parameters",
                        "Faltan parametros obligatorios: " + String.join(", ", missing));
            }
        }
        return attributes;
    }

    private ListingAttribute buildAttribute(ParameterGroup group, AttributeCommand command) {
        return switch (group.dataType()) {
            case OPTION -> {
                if (command.optionCode() == null || command.optionCode().isBlank()) {
                    yield ListingAttribute.option(group.code(), null);
                }
                parameters.requireValidOption(group.code(), command.optionCode());
                yield ListingAttribute.option(group.code(), command.optionCode());
            }
            case TEXT -> ListingAttribute.text(group.code(), trimToNull(command.textValue()));
            case NUMBER -> ListingAttribute.number(group.code(), command.numberValue());
            case BOOLEAN -> ListingAttribute.flag(group.code(), command.boolValue());
        };
    }

    private List<ListingImage> buildImages(List<ImageCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }
        if (commands.size() > maxImages()) {
            throw new BusinessRuleException(
                    "too_many_images",
                    "Maximo %d imagenes por publicacion".formatted(maxImages()));
        }
        AtomicInteger index = new AtomicInteger();
        return commands.stream()
                .filter(image -> image.url() != null && !image.url().isBlank())
                .map(image -> ListingImage.of(
                        image.url().trim(),
                        image.alt(),
                        image.position() == null ? index.getAndIncrement() : image.position()))
                .toList();
    }

    private String resolveTitle(SaveListingCommand command, VehicleModel model) {
        if (command.title() != null && !command.title().isBlank()) {
            return command.title().trim();
        }
        return "%s %s %d".formatted(model.brandName(), model.name(), command.year());
    }

    private String generateReference() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = "CAMS-" + randomToken(6);
            if (!listings.existsByReference(candidate)) {
                return candidate;
            }
        }
        throw new BusinessRuleException("No fue posible generar una referencia unica, intenta de nuevo");
    }

    private String generateSlug(VehicleModel model, int year, String title) {
        String base = Slugs.of(model.brandName(), model.name(), String.valueOf(year));
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = base + "-" + randomToken(5).toLowerCase();
            if (!listings.existsBySlug(candidate)) {
                return candidate;
            }
        }
        return Slugs.of(title, String.valueOf(System.currentTimeMillis()));
    }

    private String randomToken(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(REFERENCE_ALPHABET.charAt(RANDOM.nextInt(REFERENCE_ALPHABET.length())));
        }
        return builder.toString();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private int maxImages() {
        return properties.catalog().maxImagesPerListing();
    }

    private int minYear() {
        return properties.catalog().minYear();
    }
}
