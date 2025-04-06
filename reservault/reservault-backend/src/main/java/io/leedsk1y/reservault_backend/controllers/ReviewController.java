package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.dto.ReviewDetailedDTO;
import io.leedsk1y.reservault_backend.dto.ReviewRequestDTO;
import io.leedsk1y.reservault_backend.services.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewDetailedDTO>> getReviews(@PathVariable UUID offerId) {
        logger.info("Fetching reviews for offer ID: {}", offerId);
        return ResponseEntity.ok(reviewService.getReviewsForOffer(offerId));
    }

    @PostMapping
    public ResponseEntity<ReviewDetailedDTO> createReview(
            @PathVariable UUID offerId,
            @RequestBody ReviewRequestDTO dto) {
        logger.info("Creating review for offer ID: {}", offerId);
        return ResponseEntity.ok(reviewService.addReviewToOffer(offerId, dto));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID offerId,
            @PathVariable UUID reviewId
    ) {
        logger.info("Deleting review ID: {} from offer ID: {}", reviewId, offerId);
        reviewService.deleteReviewFromOffer(offerId, reviewId);
        return ResponseEntity.noContent().build();
    }
}