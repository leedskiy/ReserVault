package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.services.AdminService;

import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> createHotel(
        @RequestPart("hotel") String hotelJson,
        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Hotel hotel = objectMapper.readValue(hotelJson, Hotel.class);

            Hotel createdHotel = adminService.createHotel(hotel, images);
            return ResponseEntity.ok(createdHotel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getMessage());
        }
    }

    @PutMapping(value="/hotels/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateHotel(
        @PathVariable UUID id,
        @RequestPart("hotel") String hotelJson,
        @RequestPart(value = "images") List<MultipartFile> images) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Hotel updatedHotel = objectMapper.readValue(hotelJson, Hotel.class);
            Optional<Hotel> hotel = adminService.updateHotel(id, updatedHotel, images);
            return hotel.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getMessage());
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

    @GetMapping("/users")
    public ResponseEntity<List<UserDetailedResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        Optional<UserDetailedResponseDTO> user = adminService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/managers/{managerId}/approve")
    public ResponseEntity<?> approveManagerRequest(@PathVariable UUID managerId) {
        boolean approved = adminService.approveManagerRequest(managerId);
        return approved
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/managers/{managerId}/reject")
    public ResponseEntity<?> rejectManagerRequest(@PathVariable UUID managerId) {
        boolean rejected = adminService.rejectManagerRequest(managerId);
        return rejected
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/managers/{managerId}/hotels")
    public ResponseEntity<?> getHotelsByManagerList(@PathVariable UUID managerId) {
        try {
            List<HotelManager> hotelManagers = adminService.getHotelsByManagerList(managerId);
            return ResponseEntity.ok(hotelManagers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/managers/{managerId}/hotels")
    public ResponseEntity<?> updateHotelsByManagerList(
            @PathVariable UUID managerId,
            @RequestBody List<String> updatedHotelIdentifiers) {

        try {
            List<HotelManager> updatedHotelManagers = adminService.updateHotelsByManagerList(managerId, updatedHotelIdentifiers);
            return ResponseEntity.ok(updatedHotelManagers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}