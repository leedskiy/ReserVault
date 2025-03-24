package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.OfferWithLocationDTO;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ManagerService {
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final CloudinaryService cloudinaryService;
    private final HotelManagerRepository hotelManagerRepository;
    private final HotelRepository hotelRepository;

    public ManagerService(UserRepository userRepository, OfferRepository offerRepository,
                          CloudinaryService cloudinaryService, HotelManagerRepository hotelManagerRepository,
                          HotelRepository hotelRepository) {
        this.userRepository = userRepository;
        this.offerRepository = offerRepository;
        this.cloudinaryService = cloudinaryService;
        this.hotelManagerRepository = hotelManagerRepository;
        this.hotelRepository = hotelRepository;
    }

    public List<OfferWithLocationDTO> getManagerOffers() {
        User user = validateAndGetManager();
        List<Offer> offers = offerRepository.findByManagerId(user.getId());

        return offers.stream().map(offer -> {
            Hotel hotel = hotelRepository.findByIdentifier(offer.getHotelIdentifier())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found for offer"));

            return new OfferWithLocationDTO(
                    offer.getId(),
                    offer.getHotelIdentifier(),
                    offer.getTitle(),
                    offer.getDescription(),
                    offer.getRating(),
                    offer.getDateFrom(),
                    offer.getDateUntil(),
                    offer.getFacilities(),
                    offer.getRoomCount(),
                    offer.getPeopleCount(),
                    offer.getPricePerNight(),
                    offer.getImagesUrls(),
                    offer.getCreatedAt(),
                    offer.getReviews(),
                    hotel.getLocation(),
                    hotel.getName(),
                    hotel.getStars()
            );
        }).toList();
    }

    public List<HotelManager> getHotelsByManagerList() {
        User user = validateAndGetManager();

        return hotelManagerRepository.findByManagerId(user.getId());
    }

    public Offer createOffer(Offer offer, List<MultipartFile> images) throws IOException {
        User user = validateAndGetManager();

        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("At least one image is required to create an offer.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd.yyyy");
        LocalDate fromDate;
        LocalDate untilDate;
        try {
            fromDate = LocalDate.parse(offer.getDateFrom(), formatter);
            untilDate = LocalDate.parse(offer.getDateUntil(), formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Dates must be in mm.dd.yyyy format.");
        }

        if (!fromDate.isBefore(untilDate) && !fromDate.equals(untilDate)) {
            throw new IllegalArgumentException("date from must be before date until.");
        }

        HotelManager hotelManager = hotelManagerRepository
                .findByManagerIdAndHotelIdentifier(user.getId(), offer.getHotelIdentifier())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not a manager for this hotel."));

        offer.setId(UUID.randomUUID());
        offer.setManagerId(user.getId());
        offer.setHotelManagerId(hotelManager.getId());
        offer.setRating(10);
        offer.setCreatedAt(Instant.now());

        for (MultipartFile image : images) {
            String imageUrl = cloudinaryService.uploadImage(image, "offers_images");
            offer.getImagesUrls().add(imageUrl);
        }

        return offerRepository.save(offer);
    }

    public Offer updateOffer(UUID offerId, Offer updatedOffer, List<MultipartFile> newImages) throws IOException {
        User user = validateAndGetManager();

        Offer existingOffer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        if (!existingOffer.getManagerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this offer");
        }

        hotelManagerRepository.findByManagerIdAndHotelIdentifier(user.getId(), existingOffer.getHotelIdentifier())
                .orElseThrow(() -> new IllegalArgumentException("You are not a manager for the specified hotel."));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd.yyyy");
        LocalDate fromDate, untilDate;
        try {
            fromDate = LocalDate.parse(updatedOffer.getDateFrom(), formatter);
            untilDate = LocalDate.parse(updatedOffer.getDateUntil(), formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Dates must be in MM.dd.yyyy format.");
        }

        if (!fromDate.isBefore(untilDate) && !fromDate.equals(untilDate)) {
            throw new IllegalArgumentException("date from must be before date until.");
        }

        existingOffer.setTitle(updatedOffer.getTitle());
        existingOffer.setDescription(updatedOffer.getDescription());
        existingOffer.setDateFrom(updatedOffer.getDateFrom());
        existingOffer.setDateUntil(updatedOffer.getDateUntil());
        existingOffer.setFacilities(updatedOffer.getFacilities());
        existingOffer.setRoomCount(updatedOffer.getRoomCount());
        existingOffer.setPeopleCount(updatedOffer.getPeopleCount());
        existingOffer.setPricePerNight(updatedOffer.getPricePerNight());

        if (updatedOffer.getImagesUrls() != null && !updatedOffer.getImagesUrls().isEmpty()) {
            existingOffer.setImagesUrls(updatedOffer.getImagesUrls());
        }
        
        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile image : newImages) {
                String imageUrl = cloudinaryService.uploadImage(image, "hotels_images");
                existingOffer.getImagesUrls().add(imageUrl);
            }
        }

        return offerRepository.save(existingOffer);
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

    public boolean deleteOffer(UUID offerId) {
        User user = validateAndGetManager();

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        if (!offer.getManagerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this offer");
        }

        for (String imageUrl : offer.getImagesUrls()) {
            cloudinaryService.deleteImage(imageUrl, "offers_images");
        }

        offerRepository.deleteById(offerId);

        return true;
    }

    public boolean removeOfferImage(UUID offerId, String imageUrl) {
        User user = validateAndGetManager();

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        if (!offer.getManagerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to modify this offer");
        }

        if (!offer.getImagesUrls().contains(imageUrl)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image URL not found in this offer");
        }

        cloudinaryService.deleteImage(imageUrl, "offers_images");

        offer.getImagesUrls().remove(imageUrl);
        offerRepository.save(offer);

        return true;
    }
}