package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.services.AdminService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/hotels")
    public List<Hotel> getAllHotels() {
        return adminService.getAllHotels();
    }

    @PostMapping(value = "/hotels", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Hotel> createHotel(
        @RequestPart("hotel") String hotelJson,
        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Hotel hotel = objectMapper.readValue(hotelJson, Hotel.class);

            Hotel createdHotel = adminService.createHotel(hotel, images);
            return ResponseEntity.ok(createdHotel);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping(value="/hotels/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Hotel> updateHotel(
        @PathVariable UUID id,
        @RequestPart("hotel") String hotelJson,
        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Hotel updatedHotel = objectMapper.readValue(hotelJson, Hotel.class);
            Optional<Hotel> hotel = adminService.updateHotel(id, updatedHotel, images);
            return hotel.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/hotels/{id}")
    public ResponseEntity<String> deleteHotel(@PathVariable UUID id) {
        boolean deleted = adminService.deleteHotel(id);
        return deleted ? ResponseEntity.ok("Hotel deleted") : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/hotels/{hotelId}/images")
    public ResponseEntity<String> removeHotelImage(
            @PathVariable UUID hotelId,
            @RequestParam("imageUrl") String imageUrl) {

        boolean removed = adminService.removeHotelImage(hotelId, imageUrl);
        return removed
                ? ResponseEntity.ok("Image removed successfully")
                : ResponseEntity.notFound().build();
    }
}