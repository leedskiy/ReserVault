package io.leedsk1y.reservault_backend.models.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "booked_dates")
public class BookedDates {
    public BookedDates() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public BookedDates(UUID offerId, String dateFrom, String dateUntil) {
        this.id = UUID.randomUUID();
        this.offerId = offerId;
        this.dateFrom = dateFrom;
        this.dateUntil = dateUntil;
        this.createdAt = Instant.now();
    }

    @Id
    private UUID id;

    private UUID offerId;

    private String dateFrom;

    private String dateUntil;

    @CreatedDate
    private Instant createdAt;
}