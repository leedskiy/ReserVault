package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.OfferWithLocationDTO;
import io.leedsk1y.reservault_backend.models.entities.BookedDates;
import io.leedsk1y.reservault_backend.models.entities.Facilities;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.repositories.BookedDatesRepository;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public OfferService(OfferRepository offerRepository,
                        HotelRepository hotelRepository,
                        BookedDatesRepository bookedDatesRepository) {
        this.offerRepository = offerRepository;
        this.hotelRepository = hotelRepository;
        this.bookedDatesRepository = bookedDatesRepository;
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
                                                   String sortBy, String sortOrder) {
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
}
