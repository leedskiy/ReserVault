package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.ReviewDetailedDTO;
import io.leedsk1y.reservault_backend.dto.ReviewRequestDTO;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.models.entities.Review;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;

    public ReviewService(OfferRepository offerRepository, UserRepository userRepository) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all reviews for a specific offer and maps them to detailed DTOs.
     * @param offerId UUID of the offer.
     * @return List of ReviewDetailedDTO containing review and user name data.
     * @throws ResponseStatusException If the offer is not found.
     */
    public List<ReviewDetailedDTO> getReviewsForOffer(UUID offerId) {
        logger.info("Fetching reviews for offer ID: {}", offerId);
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        return offer.getReviews().stream()
                .map(review -> {
                    String userName = userRepository.findById(review.getUserId())
                            .map(User::getName)
                            .orElse("Unknown User");

                    return ReviewDetailedDTO.fromReview(review, userName);
                })
                .toList();
    }

    /**
     * Adds a new review to the specified offer from the currently authenticated user.
     * Prevents multiple reviews by the same user on the same offer.
     * @param offerId UUID of the offer being reviewed.
     * @param dto DTO containing the review data (title, comment, rating).
     * @return ReviewDetailedDTO of the newly added review.
     * @throws ResponseStatusException If the user or offer is not found, or if the user already reviewed.
     */
    public ReviewDetailedDTO addReviewToOffer(UUID offerId, ReviewRequestDTO dto) {
        logger.info("Adding review to offer ID: {} by user email from context", offerId);
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        boolean alreadyReviewed = offer.getReviews().stream()
                .anyMatch(r -> r.getUserId().equals(user.getId()));
        if (alreadyReviewed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already reviewed this offer");
        }

        Review review = new Review(user.getId(), user.getEmail(),
                dto.getTitle(), dto.getComment(), dto.getRating());

        offer.getReviews().add(review);
        offer.setRating(calculateAverageRating(offer.getReviews()));
        offerRepository.save(offer);

        return ReviewDetailedDTO.fromReview(review, user.getName());
    }

    /**
     * Deletes a specific review from an offer if it belongs to the currently authenticated user.
     * Recalculates the offer’s average rating after deletion.
     * @param offerId UUID of the offer.
     * @param reviewId UUID of the review to delete.
     * @throws ResponseStatusException If the user, offer, or review is not found or user is unauthorized.
     */
    public void deleteReviewFromOffer(UUID offerId, UUID reviewId) {
        logger.info("Deleting review ID: {} from offer ID: {} by user email from context", reviewId, offerId);
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        Review review = offer.getReviews().stream()
                .filter(r -> r.getId().equals(reviewId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        if (!review.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own review");
        }

        offer.getReviews().remove(review);
        offer.setRating(calculateAverageRating(offer.getReviews()));
        offerRepository.save(offer);
    }

    /**
     * Calculates the average rating from a list of reviews.
     * @param reviews List of Review entities.
     * @return Average rating as a double. Returns 0.0 if no reviews exist.
     */
    private double calculateAverageRating(List<Review> reviews) {
        logger.debug("Calculating average rating for {} review(s)", reviews.size());
        if (reviews.isEmpty()) {
            return 0.0;
        }

        double total = reviews.stream()
                .mapToDouble(Review::getRating)
                .sum();

        return total / reviews.size();
    }
}