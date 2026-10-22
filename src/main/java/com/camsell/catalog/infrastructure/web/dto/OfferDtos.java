package com.camsell.catalog.infrastructure.web.dto;

import com.camsell.catalog.domain.model.Offer;
import com.camsell.catalog.domain.model.OfferStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public final class OfferDtos {

    private OfferDtos() {
    }

    public record Response(
            Long id,
            Long listingId,
            String listingReference,
            String listingTitle,
            Long sellerId,
            Long buyerId,
            String buyerName,
            String buyerPhone,
            BigDecimal amount,
            String currency,
            String message,
            OfferStatus status,
            Instant createdAt,
            Instant respondedAt) {

        public static Response from(Offer offer) {
            return new Response(
                    offer.id(),
                    offer.listingId(),
                    offer.listingReference(),
                    offer.listingTitle(),
                    offer.sellerId(),
                    offer.buyerId(),
                    offer.buyerName(),
                    offer.buyerPhone(),
                    offer.amount().amount(),
                    offer.amount().currency(),
                    offer.message(),
                    offer.status(),
                    offer.createdAt(),
                    offer.respondedAt());
        }
    }

    public record Submit(
            @NotNull(message = "El monto de la oferta es obligatorio")
            @DecimalMin(value = "1", message = "El monto debe ser mayor que cero")
            BigDecimal amount,

            @Size(max = 3)
            String currency,

            @Size(max = 1000, message = "El mensaje admite maximo 1000 caracteres")
            String message,

            @Size(max = 40)
            String contactPhone) {
    }
}
