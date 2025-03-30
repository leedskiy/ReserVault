package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.dto.ReviewDetailedDTO;
import io.leedsk1y.reservault_backend.dto.ReviewRequestDTO;
import io.leedsk1y.reservault_backend.models.entities.Review;
import io.leedsk1y.reservault_backend.services.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/offers/{offerId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewDetailedDTO>> getReviews(@PathVariable UUID offerId) {
        return ResponseEntity.ok(reviewService.getReviewsForOffer(offerId));
    }

    @PostMapping
    public ResponseEntity<ReviewDetailedDTO> createReview(
            @PathVariable UUID offerId,
            @RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.ok(reviewService.addReviewToOffer(offerId, dto));
    }
}