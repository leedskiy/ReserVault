package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.dto.AdminDashboardStatsDTO;
import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.services.AdminService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Retrieves a list of all hotels in the system.
     * @return A list of Hotel entities.
     */
    @GetMapping("/hotels")
    public List<Hotel> getAllHotels() {
        logger.info("Fetching all hotels");
        return adminService.getAllHotels();
    }

    /**
     * Creates a new hotel with the provided details and images.
     * @param hotelJson JSON string representing the Hotel entity.
     * @param images List of images associated with the hotel.
     * @return ResponseEntity containing the created Hotel or an error message.
     */
    @PostMapping(value = "/hotels", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createHotel(
        @RequestPart("hotel") String hotelJson,
        @RequestPart(value = "images") List<MultipartFile> images) {
        logger.info("Creating new hotel");
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

    /**
     * Updates an existing hotel with the given ID using the provided details and optional images.
     * @param id UUID of the hotel to be updated.
     * @param hotelJson JSON string representing the updated Hotel entity.
     * @param images Optional list of new images for the hotel.
     * @return ResponseEntity containing the updated Hotel or an error message.
     */
    @PutMapping(value="/hotels/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateHotel(
        @PathVariable UUID id,
        @RequestPart("hotel") String hotelJson,
        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        logger.info("Updating hotel with ID: {}", id);
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

    /**
     * Deletes a hotel by its UUID.
     * @param id UUID of the hotel to be deleted.
     * @return ResponseEntity indicating the result of the deletion.
     */
    @DeleteMapping("/hotels/{id}")
    public ResponseEntity<String> deleteHotel(@PathVariable UUID id) {
        logger.info("Deleting hotel with ID: {}", id);
        boolean deleted = adminService.deleteHotel(id);
        return deleted ? ResponseEntity.ok("Hotel deleted") : ResponseEntity.notFound().build();
    }

    /**
     * Removes a specific image from a hotel by hotel ID and image URL.
     * @param hotelId UUID of the hotel.
     * @param imageUrl URL of the image to be removed.
     * @return ResponseEntity indicating whether the image was successfully removed.
     */
    @DeleteMapping("/hotels/{hotelId}/images")
    public ResponseEntity<String> removeHotelImage(
            @PathVariable UUID hotelId,
            @RequestParam("imageUrl") String imageUrl) {
        logger.info("Removing image from hotel ID: {}, Image URL: {}", hotelId, imageUrl);
        boolean removed = adminService.removeHotelImage(hotelId, imageUrl);
        return removed
                ? ResponseEntity.ok("Image removed successfully")
                : ResponseEntity.notFound().build();
    }

    /**
     * Retrieves a detailed list of all users.
     * @return ResponseEntity containing a list of UserDetailedResponseDTO.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDetailedResponseDTO>> getAllUsers() {
        logger.info("Fetching all users");
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * Fetches a user by their unique identifier.
     * @param id UUID of the user.
     * @return ResponseEntity with the user details or not found.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        logger.info("Fetching user by ID: {}", id);
        Optional<UserDetailedResponseDTO> user = adminService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a user based on their UUID.
     * @param id UUID of the user to be deleted.
     * @return ResponseEntity indicating success or failure.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        logger.info("Deleting user with ID: {}", id);
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Approves a hotel manager request.
     * @param managerId UUID of the manager to approve.
     * @return ResponseEntity indicating the result of the approval process.
     */
    @PutMapping("/managers/{managerId}/approve")
    public ResponseEntity<?> approveManagerRequest(@PathVariable UUID managerId) {
        logger.info("Approving manager request with ID: {}", managerId);
        boolean approved = adminService.approveManagerRequest(managerId);
        return approved
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Rejects a hotel manager request.
     * @param managerId UUID of the manager to reject.
     * @return ResponseEntity indicating the result of the rejection process.
     */
    @DeleteMapping("/managers/{managerId}/reject")
    public ResponseEntity<?> rejectManagerRequest(@PathVariable UUID managerId) {
        logger.info("Rejecting manager request with ID: {}", managerId);
        boolean rejected = adminService.rejectManagerRequest(managerId);
        return rejected
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Retrieves a list of hotels associated with a given hotel manager.
     * @param managerId UUID of the manager.
     * @return ResponseEntity containing a list of HotelManager entities or an error.
     */
    @GetMapping("/managers/{managerId}/hotels")
    public ResponseEntity<?> getHotelsByManagerList(@PathVariable UUID managerId) {
        logger.info("Fetching hotels for manager ID: {}", managerId);
        try {
            List<HotelManager> hotelManagers = adminService.getHotelsByManagerList(managerId);
            return ResponseEntity.ok(hotelManagers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Updates the list of hotels managed by a specific hotel manager.
     * @param managerId UUID of the manager.
     * @param updatedHotelIdentifiers List of hotel identifiers to assign to the manager.
     * @return ResponseEntity containing the updated list of HotelManager entities or an error.
     */
    @PutMapping("/managers/{managerId}/hotels")
    public ResponseEntity<?> updateHotelsByManagerList(
            @PathVariable UUID managerId,
            @RequestBody List<String> updatedHotelIdentifiers) {
        logger.info("Updating hotel-manager relations for manager ID: {}", managerId);
        try {
            List<HotelManager> updatedHotelManagers = adminService.updateHotelsByManagerList(managerId, updatedHotelIdentifiers);
            return ResponseEntity.ok(updatedHotelManagers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Retrieves admin dashboard statistics.
     * @return ResponseEntity containing AdminDashboardStatsDTO.
     */
    @GetMapping("/statistics")
    public ResponseEntity<AdminDashboardStatsDTO> getDashboardStats() {
        logger.info("Fetching admin dashboard statistics");
        return ResponseEntity.ok(adminService.getAdminDashboardStats());
    }
}