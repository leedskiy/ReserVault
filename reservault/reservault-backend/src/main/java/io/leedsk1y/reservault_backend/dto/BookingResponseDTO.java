package io.leedsk1y.reservault_backend.dto;

import io.leedsk1y.reservault_backend.models.entities.Location;
import io.leedsk1y.reservault_backend.models.enums.EBookingStatus;
import io.leedsk1y.reservault_backend.models.enums.EPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class BookingResponseDTO {
    private UUID id;

    private UUID offerId;

    private String offerTitle;

    private BigDecimal price;

    private String hotelName;

    private String hotelIdentifier;

    private Location hotelLocation;

    private String dateFrom;

    private String dateUntil;

    private EBookingStatus status;

    private EPaymentStatus paymentStatus;

    private Instant expiresAt;
}