package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.services.HotelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/hotels")
public class HotelController {
    private static final Logger logger = LoggerFactory.getLogger(HotelController.class);
    private HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    /**
     * Retrieves a hotel by its unique identifier.
     * @param identifier The identifier of the hotel.
     * @return ResponseEntity containing the Hotel or 404 if not found.
     */
    @GetMapping("/{identifier}")
    public ResponseEntity<Hotel> getHotelByIdentifier(@PathVariable String identifier) {
        logger.info("Fetching hotel by identifier: {}", identifier);
        Optional<Hotel> hotel = hotelService.getHotelByIdentifier(identifier);
        return hotel.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}