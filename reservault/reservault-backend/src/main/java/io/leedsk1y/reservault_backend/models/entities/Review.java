package io.leedsk1y.reservault_backend.models.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Review {
    public Review(UUID userId, String userEmail, String title, String comment, int rating) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.userEmail = userEmail;
        this.title = title;
        this.comment = comment;
        this.rating = rating;
        this.createdAt = Instant.now();
    }

    private UUID id;

    private UUID userId;

    private String userEmail;

    private String title;

    private String comment;

    private double rating;

    private Instant createdAt;

    private ReviewResponse response;
}