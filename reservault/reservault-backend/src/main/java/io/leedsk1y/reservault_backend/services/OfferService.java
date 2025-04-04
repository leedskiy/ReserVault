package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.OfferWithLocationDTO;
import io.leedsk1y.reservault_backend.dto.ReviewResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.BookedDates;
import io.leedsk1y.reservault_backend.models.entities.Booking;
import io.leedsk1y.reservault_backend.models.entities.Facilities;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.models.entities.Review;
import io.leedsk1y.reservault_backend.models.entities.ReviewResponse;
import io.leedsk1y.reservault_backend.models.enums.EHotelManagerStatus;
import io.leedsk1y.reservault_backend.repositories.BookedDatesRepository;
import io.leedsk1y.reservault_backend.repositories.BookingRepository;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OfferService {
    private final OfferRepository offerRepository;
    private final HotelRepository hotelRepository;
    private final BookedDatesRepository bookedDatesRepository;
    private final CloudinaryService cloudinaryService;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final HotelManagerRepository hotelManagerRepository;

    public OfferService(OfferRepository offerRepository,
                        HotelRepository hotelRepository,
                        BookedDatesRepository bookedDatesRepository,
                        CloudinaryService cloudinaryService,
                        BookingRepository bookingRepository,
                        BookingService bookingService,
                        HotelManagerRepository hotelManagerRepository) {
        this.offerRepository = offerRepository;
        this.hotelRepository = hotelRepository;
        this.bookedDatesRepository = bookedDatesRepository;
        this.cloudinaryService = cloudinaryService;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.hotelManagerRepository = hotelManagerRepository;
    }

    public List<OfferWithLocationDTO> getAllOffers() {
        return offerRepository.findAll().stream()
                .map(this::toOfferWithLocationDTO)
                .toList();
    }

    public Optional<OfferWithLocationDTO> getOfferById(UUID id) {
        return offerRepository.findById(id)
                .map(this::toOfferWithLocationDTO);
    }

    public List<OfferWithLocationDTO> searchOffers(String location, Integer rooms, Integer people, String dateFrom, String dateUntil,
                                                   Double minPrice, Double maxPrice, Boolean wifi, Boolean parking, Boolean pool,
                                                   Boolean airConditioning, Boolean breakfast, Integer rating, Integer hotelStars,
                                                   String sortBy, String sortOrder, String hotelId) {
        List<Offer> allOffers = offerRepository.findAll();

        final String inputCity;
        final String inputCountry;
        final String rawLocation = location != null ? location.trim().toLowerCase() : null;

        if (rawLocation != null && rawLocation.contains(",")) {
            String[] parts = rawLocation.split(",", 2);
            inputCity = parts[0].trim();
            inputCountry = parts[1].trim();
        } else {
            inputCity = null;
            inputCountry = rawLocation;
        }

        // filtering
        List<Offer> filteredOffers = allOffers.stream()
                .filter(offer -> {
                    Hotel hotel = hotelRepository.findByIdentifier(offer.getHotelIdentifier()).orElse(null);
                    if (hotel == null) return false;

                    if (hotelId != null && !hotelId.equalsIgnoreCase(hotel.getIdentifier())) return false;

                    boolean matchesLocation = matchesLocation(hotel, inputCity, inputCountry);
                    boolean matchesRooms = rooms == null || offer.getRoomCount() >= rooms;
                    boolean matchesPeople = people == null || offer.getPeopleCount() >= people;
                    boolean matchesDates = checkDateRange(offer, dateFrom, dateUntil);
                    boolean matchesPrice = (minPrice == null || offer.getPricePerNight().compareTo(BigDecimal.valueOf(minPrice)) >= 0) &&
                            (maxPrice == null || offer.getPricePerNight().compareTo(BigDecimal.valueOf(maxPrice)) <= 0);
                    boolean matchesFacilities = matchesFacilities(offer, wifi, parking, pool, airConditioning, breakfast);
                    boolean matchesRating = rating == null || offer.getRating() >= rating;
                    boolean matchesHotelStars = hotelStars == null || hotel.getStars() >= hotelStars;

                    return matchesLocation && matchesRooms && matchesPeople && matchesDates && matchesPrice &&
                            matchesFacilities && matchesRating && matchesHotelStars;
                })
                .collect(Collectors.toList());

        // sorting
        Comparator<Offer> comparator = null;
        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "price":
                    comparator = Comparator.comparing(Offer::getPricePerNight);
                    break;
                case "rating":
                    comparator = Comparator.comparing(Offer::getRating);
                    break;
                case "stars":
                    comparator = Comparator.comparing(offer -> hotelRepository.findByIdentifier(offer.getHotelIdentifier())
                            .map(Hotel::getStars).orElse(0));
                    break;
                default:
                    break;
            }

            if (comparator != null && "desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }

            filteredOffers.sort(comparator);
        }

        return filteredOffers.stream()
                .map(this::toOfferWithLocationDTO)
                .collect(Collectors.toList());
    }

    private boolean matchesLocation(Hotel hotel, String inputCity, String inputCountry) {
        if (inputCity == null && inputCountry == null) return true;

        String hotelCity = hotel.getLocation().getCity().toLowerCase();
        String hotelCountry = hotel.getLocation().getCountry().toLowerCase();

        return (inputCity != null && (hotelCity.contains(inputCity) || hotelCountry.contains(inputCity)))
                || (inputCountry != null && hotelCountry.contains(inputCountry));
    }

    private boolean checkDateRange(Offer offer, String dateFrom, String dateUntil) {
        if (dateFrom != null && dateUntil != null) {
            try {
                DateTimeFormatter format = DateTimeFormatter.ofPattern("MM.dd.yyyy");

                LocalDate reqFrom = LocalDate.parse(dateFrom, format);
                LocalDate reqUntil = LocalDate.parse(dateUntil, format);
                LocalDate offerFrom = LocalDate.parse(offer.getDateFrom(), format);
                LocalDate offerUntil = LocalDate.parse(offer.getDateUntil(), format);

                return !(offerUntil.isBefore(reqFrom) || offerFrom.isAfter(reqUntil));
            } catch (DateTimeParseException e) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesFacilities(Offer offer, Boolean wifi, Boolean parking, Boolean pool, Boolean airConditioning, Boolean breakfast) {
        Facilities facilities = offer.getFacilities();

        if (wifi != null && wifi && !facilities.isWifi()) return false;
        if (parking != null && parking && !facilities.isParking()) return false;
        if (pool != null && pool && !facilities.isPool()) return false;
        if (airConditioning != null && airConditioning && !facilities.isAirConditioning()) return false;
        if (breakfast != null && breakfast && !facilities.isBreakfast()) return false;

        return true;
    }

    private OfferWithLocationDTO toOfferWithLocationDTO(Offer offer) {
        Hotel hotel = hotelRepository.findByIdentifier(offer.getHotelIdentifier()).orElse(null);

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
                hotel != null ? hotel.getLocation() : null,
                hotel != null ? hotel.getName() : "Unknown Hotel",
                hotel != null ? hotel.getStars() : 0
        );
    }

    public List<LocalDate> getBookedDatesForOffer(UUID offerId) {
        List<BookedDates> ranges = bookedDatesRepository.findByOfferId(offerId);

        List<LocalDate> allBookedDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd.yyyy");

        for (BookedDates range : ranges) {
            LocalDate start = LocalDate.parse(range.getDateFrom(), formatter);
            LocalDate end = LocalDate.parse(range.getDateUntil(), formatter);

            while (!start.isAfter(end)) {
                allBookedDates.add(start);
                start = start.plusDays(1);
            }
        }

        return allBookedDates;
    }

    public List<OfferWithLocationDTO> getOffersByManager(UUID managerId) {
        return offerRepository.findByManagerId(managerId).stream()
                .map(this::toOfferWithLocationDTO)
                .collect(Collectors.toList());
    }

    public Offer createOffer(Offer offer, List<MultipartFile> images, UUID managerId) throws IOException {
        validateManagerHotelAssociation(offer.getHotelIdentifier(), managerId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd.yyyy");
        LocalDate fromDate;
        LocalDate untilDate;
        try {
            fromDate = LocalDate.parse(offer.getDateFrom(), formatter);
            untilDate = LocalDate.parse(offer.getDateUntil(), formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Dates must be in MM.dd.yyyy format.");
        }

        if (!fromDate.isBefore(untilDate) && !fromDate.equals(untilDate)) {
            throw new IllegalArgumentException("date from must be before date until.");
        }

        offer.setId(UUID.randomUUID());
        offer.setManagerId(managerId);
        offer.setRating(10);
        offer.setCreatedAt(Instant.now());

        for (MultipartFile image : images) {
            String imageUrl = cloudinaryService.uploadImage(image, "offers_images");
            offer.getImagesUrls().add(imageUrl);
        }

        return offerRepository.save(offer);
    }

    public Offer updateOffer(UUID offerId, Offer updatedOffer, List<MultipartFile> newImages, UUID managerId) throws IOException {
        Offer existingOffer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found"));

        if (!existingOffer.getManagerId().equals(managerId)) {
            throw new IllegalArgumentException("You are not authorized to update this offer");
        }

        validateManagerHotelAssociation(updatedOffer.getHotelIdentifier(), managerId);

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

        List<BookedDates> bookedDates = bookedDatesRepository.findByOfferId(offerId);
        for (BookedDates booked : bookedDates) {
            LocalDate bookedStart = LocalDate.parse(booked.getDateFrom(), formatter);
            LocalDate bookedEnd = LocalDate.parse(booked.getDateUntil(), formatter);
            if (!(fromDate.isBefore(bookedStart) || fromDate.equals(bookedStart)) || !(untilDate.isAfter(bookedEnd) || untilDate.equals(bookedEnd))) {
                throw new IllegalArgumentException("The updated offer dates must include the already booked dates: " +
                        "From " + booked.getDateFrom() + " to " + booked.getDateUntil());
            }
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
                String imageUrl = cloudinaryService.uploadImage(image, "offers_images");
                existingOffer.getImagesUrls().add(imageUrl);
            }
        }

        return offerRepository.save(existingOffer);
    }

    public boolean deleteOffer(UUID offerId, UUID managerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found"));

        if (managerId != null && !offer.getManagerId().equals(managerId)) {
            throw new IllegalArgumentException("You are not authorized to delete this offer");
        }

        List<Booking> relatedBookings = bookingRepository.findByOfferId(offerId);
        for (Booking booking : relatedBookings) {
            bookingService.deleteBooking(booking.getId());
        }

        for (String imageUrl : offer.getImagesUrls()) {
            cloudinaryService.deleteImage(imageUrl, "offers_images");
        }

        offerRepository.deleteById(offerId);

        return true;
    }

    public boolean removeOfferImage(UUID offerId, String imageUrl, UUID managerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found"));

        if (!offer.getManagerId().equals(managerId)) {
            throw new IllegalArgumentException("You are not authorized to modify this offer");
        }

        if (!offer.getImagesUrls().contains(imageUrl)) {
            throw new IllegalArgumentException("Image URL not found in this offer");
        }

        cloudinaryService.deleteImage(imageUrl, "offers_images");

        offer.getImagesUrls().remove(imageUrl);
        offerRepository.save(offer);
        return true;
    }

    public void respondToReview(UUID reviewId, ReviewResponseDTO dto, UUID managerId) {
        Offer offer = offerRepository.findAll().stream()
                .filter(o -> o.getReviews().stream().anyMatch(r -> r.getId().equals(reviewId)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!offer.getManagerId().equals(managerId)) {
            throw new IllegalArgumentException("You are not allowed to respond to this review");
        }

        Review review = offer.getReviews().stream()
                .filter(r -> r.getId().equals(reviewId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (review.getResponse() != null) {
            throw new IllegalArgumentException("This review already has a response");
        }

        review.setResponse(new ReviewResponse(managerId, dto.getComment()));
        offerRepository.save(offer);
    }

    public void deleteReviewResponse(UUID reviewId, UUID managerId) {
        Offer offer = offerRepository.findAll().stream()
                .filter(o -> o.getReviews().stream().anyMatch(r -> r.getId().equals(reviewId)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!offer.getManagerId().equals(managerId)) {
            throw new IllegalArgumentException("You are not authorized to modify this review");
        }

        Review review = offer.getReviews().stream()
                .filter(r -> r.getId().equals(reviewId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (review.getResponse() == null) {
            throw new IllegalArgumentException("This review has no response to delete");
        }

        review.setResponse(null);
        offerRepository.save(offer);
    }

    public List<Offer> getOffersByManagerEntities(UUID managerId) {
        return offerRepository.findByManagerId(managerId);
    }

    private void validateManagerHotelAssociation(String hotelIdentifier, UUID managerId) {
        boolean isApproved = hotelManagerRepository
                .findByManagerId(managerId).stream()
                .anyMatch(hm -> hm.getHotelIdentifier().equals(hotelIdentifier)
                        && hm.getStatus() == EHotelManagerStatus.APPROVED);

        if (!isApproved) {
            throw new IllegalArgumentException("You are not approved to manage this hotel.");
        }
    }
}
