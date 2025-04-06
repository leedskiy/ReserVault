package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.ManagerDashboardStatsDTO;
import io.leedsk1y.reservault_backend.dto.OfferWithLocationDTO;
import io.leedsk1y.reservault_backend.dto.ReviewResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.models.enums.EHotelManagerStatus;
import io.leedsk1y.reservault_backend.repositories.BookingRepository;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ManagerService {
    private static final Logger logger = LoggerFactory.getLogger(ManagerService.class);
    private final UserRepository userRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final OfferService offerService;

    public ManagerService(UserRepository userRepository,
                          HotelManagerRepository hotelManagerRepository,
                          HotelRepository hotelRepository,
                          BookingRepository bookingRepository,
                          OfferService offerService) {
        this.userRepository = userRepository;
        this.hotelManagerRepository = hotelManagerRepository;
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
        this.offerService = offerService;
    }

    public List<OfferWithLocationDTO> getManagerOffers() {
        logger.info("Fetching offers for authenticated manager");
        User user = validateAndGetManager();
        return offerService.getOffersByManager(user.getId());
    }

    public List<HotelManager> getHotelsByManagerList() {
        logger.info("Fetching hotel assignments for authenticated manager");
        User user = validateAndGetManager();
        return hotelManagerRepository.findByManagerId(user.getId());
    }

    public List<HotelManager> updateManagerHotelList(List<String> updatedHotelIdentifiers) {
        logger.info("Updating hotel list for manager with {} identifiers", updatedHotelIdentifiers.size());
        User manager = validateAndGetManager();

        Set<String> validHotelIdentifiers = hotelRepository.findAll()
                .stream()
                .map(Hotel::getIdentifier)
                .collect(Collectors.toSet());

        List<String> invalidIdentifiers = updatedHotelIdentifiers.stream()
                .filter(id -> !validHotelIdentifiers.contains(id))
                .collect(Collectors.toList());

        if (!invalidIdentifiers.isEmpty()) {
            throw new IllegalArgumentException("Invalid hotel identifiers: " + invalidIdentifiers);
        }

        List<HotelManager> currentHotelManagers = hotelManagerRepository.findByManagerId(manager.getId());
        Set<String> currentIdentifiers = currentHotelManagers.stream()
                .map(HotelManager::getHotelIdentifier)
                .collect(Collectors.toSet());

        List<String> toRemove = currentIdentifiers.stream()
                .filter(id -> !updatedHotelIdentifiers.contains(id))
                .collect(Collectors.toList());

        List<String> toAdd = updatedHotelIdentifiers.stream()
                .filter(id -> !currentIdentifiers.contains(id))
                .collect(Collectors.toList());

        if (currentHotelManagers.size() - toRemove.size() + toAdd.size() < 1) {
            throw new IllegalArgumentException("You must be assigned to at least one hotel.");
        }

        hotelManagerRepository.deleteByManagerIdAndHotelIdentifierIn(manager.getId(), toRemove);

        for (String identifier : toAdd) {
            HotelManager newHotelManager = new HotelManager(identifier, manager.getId(), EHotelManagerStatus.PENDING);
            hotelManagerRepository.save(newHotelManager);
        }

        return hotelManagerRepository.findByManagerId(manager.getId());
    }

    public Offer createOffer(Offer offer, List<MultipartFile> images) throws IOException {
        logger.info("Creating new offer for manager");
        User user = validateAndGetManager();
        return offerService.createOffer(offer, images, user.getId());
    }

    public Offer updateOffer(UUID offerId, Offer updatedOffer, List<MultipartFile> newImages) throws IOException {
        logger.info("Updating offer with ID: {}", offerId);
        User user = validateAndGetManager();
        return offerService.updateOffer(offerId, updatedOffer, newImages, user.getId());
    }

    public boolean deleteOffer(UUID offerId) {
        logger.info("Deleting offer with ID: {}", offerId);
        User user = validateAndGetManager();
        return offerService.deleteOffer(offerId, user.getId());
    }

    public boolean removeOfferImage(UUID offerId, String imageUrl) {
        logger.info("Removing image from offer ID: {}, image URL: {}", offerId, imageUrl);
        User user = validateAndGetManager();
        return offerService.removeOfferImage(offerId, imageUrl, user.getId());
    }

    public void respondToReview(UUID reviewId, ReviewResponseDTO dto) {
        logger.info("Manager responding to review ID: {}", reviewId);
        User manager = validateAndGetManager();
        offerService.respondToReview(reviewId, dto, manager.getId());
    }

    public void deleteReviewResponse(UUID reviewId) {
        logger.info("Deleting review response for review ID: {}", reviewId);
        User manager = validateAndGetManager();
        offerService.deleteReviewResponse(reviewId, manager.getId());
    }

    private User validateAndGetManager() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getRoles().contains("ROLE_MANAGER") || !user.isVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: not a verified manager");
        }

        return user;
    }

    public ManagerDashboardStatsDTO getManagerDashboardStats() {
        logger.info("Fetching dashboard statistics for manager");
        User manager = validateAndGetManager();

        List<Offer> managerOffers = offerService.getOffersByManagerEntities(manager.getId());
        long offersCount = managerOffers.size();

        long bookingCount = 0;
        long totalReviews = 0;
        long respondedReviews = 0;

        for (Offer offer : managerOffers) {
            bookingCount += bookingRepository.countByOfferId(offer.getId());
            
            if (offer.getReviews() != null) {
                totalReviews += offer.getReviews().size();
                respondedReviews += offer.getReviews().stream().filter(r -> r.getResponse() != null).count();
            }
        }

        double responseRate = totalReviews > 0 ? (respondedReviews * 100.0 / totalReviews) : 0.0;

        return new ManagerDashboardStatsDTO(offersCount, bookingCount, totalReviews, responseRate);
    }
}