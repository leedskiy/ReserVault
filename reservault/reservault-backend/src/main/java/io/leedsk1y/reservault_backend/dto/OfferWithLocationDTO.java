package io.leedsk1y.reservault_backend.dto;

import io.leedsk1y.reservault_backend.models.entities.Location;
import io.leedsk1y.reservault_backend.models.entities.Facilities;
import io.leedsk1y.reservault_backend.models.entities.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferWithLocationDTO {
    private UUID id;
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
    private Instant createdAt;
    private List<Review> reviews;
    private Location location;
    private String hotelName;
    private int stars;
}