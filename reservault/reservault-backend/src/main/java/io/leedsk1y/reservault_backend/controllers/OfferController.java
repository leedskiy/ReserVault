package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.dto.OfferWithLocationDTO;
import io.leedsk1y.reservault_backend.services.OfferService;
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
    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public ResponseEntity<List<OfferWithLocationDTO>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOfferById(@PathVariable UUID id) {
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
            @RequestParam(required = false) String sortOrder
    ) {
        return ResponseEntity.ok(
                offerService.searchOffers(location, rooms, people, dateFrom, dateUntil,
                        minPrice, maxPrice, wifi, parking, pool, airConditioning,
                        breakfast, rating, hotelStars, sortBy, sortOrder)
        );
    }

    @GetMapping("/{offerId}/booked-dates")
    public ResponseEntity<?> getBookedDatesForOffer(@PathVariable UUID offerId) {
        return ResponseEntity.ok(offerService.getBookedDatesForOffer(offerId));
    }
}