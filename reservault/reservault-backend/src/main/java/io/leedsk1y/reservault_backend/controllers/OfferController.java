package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.dto.OfferWithLocationDTO;
import io.leedsk1y.reservault_backend.services.OfferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/offers")
public class OfferController {
    private static final Logger logger = LoggerFactory.getLogger(OfferController.class);
    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    /**
     * Retrieves all offers in the system along with associated hotel location and metadata.
     * @return ResponseEntity containing a list of OfferWithLocationDTOs.
     */
    @GetMapping
    public ResponseEntity<List<OfferWithLocationDTO>> getAllOffers() {
        logger.info("Fetching all offers");
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    /**
     * Retrieves an offer by its UUID.
     * @param id UUID of the offer to fetch.
     * @return ResponseEntity with the offer data if found, or 404 if not.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOfferById(@PathVariable UUID id) {
        logger.info("Fetching offer by ID: {}", id);
        return offerService.getOfferById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Searches offers based on filters like location, room count, date range, price, facilities, and sorting.
     * @param location The city or country to search in.
     * @param rooms Minimum required number of rooms.
     * @param people Minimum number of people the offer must accommodate.
     * @param dateFrom Start date of the desired booking range (MM.dd.yyyy).
     * @param dateUntil End date of the desired booking range (MM.dd.yyyy).
     * @param minPrice Minimum nightly price filter.
     * @param maxPrice Maximum nightly price filter.
     * @param wifi Filter for Wi-Fi facility.
     * @param parking Filter for parking facility.
     * @param pool Filter for pool facility.
     * @param airConditioning Filter for air conditioning.
     * @param breakfast Filter for breakfast availability.
     * @param rating Minimum rating filter.
     * @param hotelStars Minimum hotel star rating.
     * @param sortBy Attribute to sort results by (e.g. price, rating).
     * @param sortOrder Sorting order: "asc" or "desc".
     * @param hotelId Filter offers by a specific hotel identifier.
     * @return ResponseEntity with filtered and sorted list of offers.
     */
    @GetMapping("/search")
    public ResponseEntity<List<OfferWithLocationDTO>> searchOffers(
            @RequestParam String location,
            @RequestParam Integer rooms,
            @RequestParam Integer people,
            @RequestParam String dateFrom,
            @RequestParam String dateUntil,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean wifi,
            @RequestParam(required = false) Boolean parking,
            @RequestParam(required = false) Boolean pool,
            @RequestParam(required = false) Boolean airConditioning,
            @RequestParam(required = false) Boolean breakfast,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer hotelStars,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) String hotelId
    ) {
        logger.info("Searching offers for location: {}, from: {}, until: {}", location, dateFrom, dateUntil);
        return ResponseEntity.ok(
                offerService.searchOffers(location, rooms, people, dateFrom, dateUntil,
                        minPrice, maxPrice, wifi, parking, pool, airConditioning,
                        breakfast, rating, hotelStars, sortBy, sortOrder, hotelId)
        );
    }

    /**
     * Retrieves all dates that are currently booked for a specific offer.
     * @param offerId UUID of the offer.
     * @return ResponseEntity containing a list of LocalDate objects representing booked dates.
     */
    @GetMapping("/{offerId}/booked-dates")
    public ResponseEntity<?> getBookedDatesForOffer(@PathVariable UUID offerId) {
        logger.info("Fetching booked dates for offer ID: {}", offerId);
        return ResponseEntity.ok(offerService.getBookedDatesForOffer(offerId));
    }
}