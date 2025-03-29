package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.dto.BookingResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.Booking;
import io.leedsk1y.reservault_backend.services.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        try {
            Booking saved = bookingService.createBooking(booking);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Booking failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getUserBookings() {
        return ResponseEntity.ok(bookingService.getUserBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable UUID id) {
        Optional<Booking> booking = bookingService.getBookingById(id);
        return booking.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable UUID id) {
        try {
            boolean result = bookingService.cancelBooking(id);
            return result ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> simulatePayment(@PathVariable UUID id) {
        try {
            Booking updated = bookingService.simulatePayment(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/payment-status")
    public ResponseEntity<?> getPaymentStatus(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(bookingService.getPaymentStatus(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.GONE).body(e.getMessage());
        }
    }
}