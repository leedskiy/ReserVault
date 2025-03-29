package io.leedsk1y.reservault_backend.models.entities;

import io.leedsk1y.reservault_backend.models.enums.EBookingStatus;
import io.leedsk1y.reservault_backend.models.enums.EPaymentStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "bookings")
public class Booking {
    public Booking() {
        this.createdAt = Instant.now();
        this.expiresAt = Instant.now().plusSeconds(3600);
        this.status = EBookingStatus.PENDING;
    }

    @Id
    private UUID id;

    private UUID offerId;

    private UUID userId;

    private String dateFrom;

    private String dateUntil;

    private EBookingStatus status;

    @CreatedDate
    private Instant createdAt;

    private Instant expiresAt;

    private Payment payment;
}