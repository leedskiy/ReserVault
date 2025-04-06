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

    @GetMapping
    public ResponseEntity<List<OfferWithLocationDTO>> getAllOffers() {
        logger.info("Fetching all offers");
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOfferById(@PathVariable UUID id) {
        logger.info("Fetching offer by ID: {}", id);
        return offerService.getOfferById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

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

    @GetMapping("/{offerId}/booked-dates")
    public ResponseEntity<?> getBookedDatesForOffer(@PathVariable UUID offerId) {
        logger.info("Fetching booked dates for offer ID: {}", offerId);
        return ResponseEntity.ok(offerService.getBookedDatesForOffer(offerId));
    }
}