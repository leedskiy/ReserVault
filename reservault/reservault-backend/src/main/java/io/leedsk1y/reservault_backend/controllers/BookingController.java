package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.dto.BookingResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.Booking;
import io.leedsk1y.reservault_backend.services.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Creates a new booking for the authenticated user.
     * @param booking The booking request payload.
     * @return ResponseEntity containing the saved booking or an error message.
     */
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        logger.info("Creating booking for user");
        try {
            Booking saved = bookingService.createBooking(booking);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Booking failed: " + e.getMessage());
        }
    }

    /**
     * Retrieves all bookings made by the currently authenticated user.
     * @return ResponseEntity with a list of BookingResponseDTOs.
     */
    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getUserBookings() {
        logger.info("Fetching bookings for current user");
        return ResponseEntity.ok(bookingService.getUserBookings());
    }

    /**
     * Retrieves a specific booking by its ID if it belongs to the current user.
     * @param id UUID of the booking.
     * @return ResponseEntity with the booking or 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable UUID id) {
        logger.info("Fetching booking by ID: {}", id);
        Optional<Booking> booking = bookingService.getBookingById(id);
        return booking.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Cancels a pending and unpaid booking by its ID if owned by the user.
     * @param id UUID of the booking to cancel.
     * @return ResponseEntity indicating success, not found, or error.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable UUID id) {
        logger.info("Cancelling booking with ID: {}", id);
        try {
            boolean result = bookingService.cancelBooking(id);
            return result ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Simulates a successful payment for a pending booking.
     * @param id UUID of the booking to simulate payment for.
     * @return ResponseEntity with the updated booking or an error.
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<?> simulatePayment(@PathVariable UUID id) {
        logger.info("Simulating payment for booking ID: {}", id);
        try {
            Booking updated = bookingService.simulatePayment(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieves the payment status for a specific booking.
     * @param id UUID of the booking.
     * @return ResponseEntity with the payment status or error if booking expired.
     */
    @GetMapping("/{id}/payment-status")
    public ResponseEntity<?> getPaymentStatus(@PathVariable UUID id) {
        logger.info("Fetching payment status for booking ID: {}", id);
        try {
            return ResponseEntity.ok(bookingService.getPaymentStatus(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.GONE).body(e.getMessage());
        }
    }
}