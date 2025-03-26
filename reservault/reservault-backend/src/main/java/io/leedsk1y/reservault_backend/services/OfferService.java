package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.OfferWithLocationDTO;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OfferService {
    private final OfferRepository offerRepository;
    private final HotelRepository hotelRepository;

    public OfferService(OfferRepository offerRepository, HotelRepository hotelRepository) {
        this.offerRepository = offerRepository;
        this.hotelRepository = hotelRepository;
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

    public List<OfferWithLocationDTO> searchOffers(String location, Integer rooms, Integer people, String dateFrom, String dateUntil) {
        List<Offer> allOffers = offerRepository.findAll();

        return allOffers.stream()
                .filter(offer -> {
                    Hotel hotel = hotelRepository.findByIdentifier(offer.getHotelIdentifier()).orElse(null);
                    if (hotel == null) return false;

                    boolean matchesLocation = true;
                    if (location != null && !location.isBlank()) {
                        String loc = location.toLowerCase();
                        matchesLocation = hotel.getLocation().getCity().toLowerCase().contains(loc)
                                || hotel.getLocation().getCountry().toLowerCase().contains(loc);
                    }

                    boolean matchesRooms = rooms == null || offer.getRoomCount() >= rooms;
                    boolean matchesPeople = people == null || offer.getPeopleCount() >= people;

                    boolean matchesDates = true;
                    if (dateFrom != null && dateUntil != null) {
                        try {
                            DateTimeFormatter frontendFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            DateTimeFormatter offerFormat = DateTimeFormatter.ofPattern("MM.dd.yyyy");

                            LocalDate reqFrom = LocalDate.parse(dateFrom, frontendFormat);
                            LocalDate reqUntil = LocalDate.parse(dateUntil, frontendFormat);

                            LocalDate offerFrom = LocalDate.parse(offer.getDateFrom(), offerFormat);
                            LocalDate offerUntil = LocalDate.parse(offer.getDateUntil(), offerFormat);

                            matchesDates = !(offerUntil.isBefore(reqFrom) || offerFrom.isAfter(reqUntil));
                        } catch (DateTimeParseException e) {
                            return false;
                        }
                    }

                    return matchesLocation && matchesRooms && matchesPeople && matchesDates;
                })
                .map(this::toOfferWithLocationDTO)
                .toList();
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
}
