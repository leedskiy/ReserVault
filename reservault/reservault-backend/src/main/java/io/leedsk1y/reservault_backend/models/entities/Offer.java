package io.leedsk1y.reservault_backend.models.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "offers")
public class Offer {
    public Offer() {
        this.imagesUrls = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    @Id
    private UUID id;

    private UUID hotelManagerId;

    private String hotelIdentifier;

    private UUID managerId;

    private String title;

    private String description;

    private double rating;

    private String dateFrom;

    private String dateUntil;

    private Facilities facilities;

    private int roomCount;

    private int peopleCount;

    private BigDecimal pricePerNight;

    private List<String> imagesUrls;

    @CreatedDate
    private Instant createdAt;

    private List<Review> reviews;
}
