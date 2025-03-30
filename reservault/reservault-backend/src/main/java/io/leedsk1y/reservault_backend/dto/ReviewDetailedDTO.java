package io.leedsk1y.reservault_backend.dto;

import io.leedsk1y.reservault_backend.models.entities.Review;
import io.leedsk1y.reservault_backend.models.entities.ReviewResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ReviewDetailedDTO {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userName;
    private String title;
    private String comment;
    private double rating;
    private Instant createdAt;
    private ReviewResponse response;

    public static ReviewDetailedDTO fromReview(Review review, String userName) {
        return new ReviewDetailedDTO(
                review.getId(),
                review.getUserId(),
                review.getUserEmail(),
                userName,
                review.getTitle(),
                review.getComment(),
                review.getRating(),
                review.getCreatedAt(),
                review.getResponse()
        );
    }
}