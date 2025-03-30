package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.ReviewDetailedDTO;
import io.leedsk1y.reservault_backend.dto.ReviewRequestDTO;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.models.entities.Review;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;

    public ReviewService(OfferRepository offerRepository, UserRepository userRepository) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
    }

    public List<ReviewDetailedDTO> getReviewsForOffer(UUID offerId) {
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

    public ReviewDetailedDTO addReviewToOffer(UUID offerId, ReviewRequestDTO dto) {
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
        offerRepository.save(offer);

        return ReviewDetailedDTO.fromReview(review, user.getName());
    }
}