package io.leedsk1y.reservault_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leedsk1y.reservault_backend.dto.ManagerDashboardStatsDTO;
import io.leedsk1y.reservault_backend.dto.OfferWithLocationDTO;
import io.leedsk1y.reservault_backend.dto.ReviewResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.services.ManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.UUID;

@RestController
@RequestMapping("/manager")
public class ManagerController {
    private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);
    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    /**
     * Retrieves all offers associated with the currently authenticated manager.
     * @return ResponseEntity containing a list of OfferWithLocationDTOs.
     */
    @GetMapping("/offers")
    public ResponseEntity<List<OfferWithLocationDTO>> getManagerOffers() {
        logger.info("Fetching manager's offers");
        return ResponseEntity.ok(managerService.getManagerOffers());
    }

    /**
     * Retrieves a list of hotels assigned to the authenticated manager.
     * @return ResponseEntity with the manager's HotelManager associations or an error message.
     */
    @GetMapping("/hotels")
    public ResponseEntity<?> getHotelsByManagerList() {
        logger.info("Fetching hotels assigned to current manager");
        try {
            List<HotelManager> hotelManagers = managerService.getHotelsByManagerList();
            return ResponseEntity.ok(hotelManagers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Updates the list of hotels assigned to the authenticated manager.
     * @param updatedHotelIdentifiers A list of new hotel identifiers to assign.
     * @return ResponseEntity with the updated HotelManager list or error details.
     */
    @PutMapping("/hotels")
    public ResponseEntity<?> updateManagerHotelList(@RequestBody List<String> updatedHotelIdentifiers) {
        logger.info("Updating manager's hotel list with {} identifiers", updatedHotelIdentifiers.size());
        try {
            List<HotelManager> updated = managerService.updateManagerHotelList(updatedHotelIdentifiers);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Creates a new offer for the manager using provided offer data and images.
     * @param offerJson JSON string representing the Offer object.
     * @param images List of image files to associate with the offer.
     * @return ResponseEntity with the created Offer or error message.
     */
    @PostMapping(value="/offers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createOffer(
            @RequestPart("offer") String offerJson,
            @RequestPart(value = "images") List<MultipartFile> images) {
        logger.info("Creating a new offer for manager");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Offer offer = objectMapper.readValue(offerJson, Offer.class);
            Offer createdOffer = managerService.createOffer(offer, images);
            return ResponseEntity.ok(createdOffer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * Updates an existing offer for the manager using provided data and optional images.
     * @param id UUID of the offer to update.
     * @param offerJson JSON string of updated offer data.
     * @param images Optional list of new images to add.
     * @return ResponseEntity with the updated Offer or error message.
     */
    @PutMapping(value = "/offers/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateOffer(
            @PathVariable UUID id,
            @RequestPart("offer") String offerJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        logger.info("Updating offer with ID: {}", id);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Offer updatedOffer = objectMapper.readValue(offerJson, Offer.class);
            Offer savedOffer = managerService.updateOffer(id, updatedOffer, images);
            return ResponseEntity.ok(savedOffer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * Deletes an offer owned by the manager by its ID.
     * @param id UUID of the offer to delete.
     * @return ResponseEntity indicating success or failure.
     */
    @DeleteMapping(value="/offers/{id}")
    public ResponseEntity<?> deleteOffer(@PathVariable UUID id) {
        logger.info("Deleting offer with ID: {}", id);
        try {
            managerService.deleteOffer(id);
            return ResponseEntity.ok().body("Offer deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Removes an image from a specific offer.
     * @param offerId UUID of the offer.
     * @param imageUrl URL of the image to remove.
     * @return ResponseEntity indicating success or 404 if not found.
     */
    @DeleteMapping("/offers/{offerId}/images")
    public ResponseEntity<String> removeOfferImage(
            @PathVariable UUID offerId,
            @RequestParam("imageUrl") String imageUrl) {
        logger.info("Removing image from offer ID: {}, Image URL: {}", offerId, imageUrl);
        boolean removed = managerService.removeOfferImage(offerId, imageUrl);
        return removed
                ? ResponseEntity.ok("Image removed successfully")
                : ResponseEntity.notFound().build();
    }

    /**
     * Adds a manager's response to a user review on one of their offers.
     * @param reviewId UUID of the review to respond to.
     * @param dto DTO containing the response text.
     * @return ResponseEntity confirming the response was added.
     */
    @PostMapping("/reviews/{reviewId}/response")
    public ResponseEntity<?> respondToReview(
            @PathVariable UUID reviewId,
            @RequestBody ReviewResponseDTO dto) {
        logger.info("Manager responding to review ID: {}", reviewId);
        managerService.respondToReview(reviewId, dto);
        return ResponseEntity.ok("Response added successfully");
    }

    /**
     * Deletes the manager's response to a specific review.
     * @param reviewId UUID of the review.
     * @return ResponseEntity confirming deletion.
     */
    @DeleteMapping("/reviews/{reviewId}/response")
    public ResponseEntity<?> deleteResponseToReview(@PathVariable UUID reviewId) {
        logger.info("Deleting manager response to review ID: {}", reviewId);
        managerService.deleteReviewResponse(reviewId);
        return ResponseEntity.ok("Response deleted successfully");
    }

    /**
     * Retrieves dashboard statistics for the currently authenticated manager.
     * @return ResponseEntity containing ManagerDashboardStatsDTO.
     */
    @GetMapping("/statistics")
    public ResponseEntity<ManagerDashboardStatsDTO> getManagerStats() {
        logger.info("Fetching dashboard statistics for manager");
        return ResponseEntity.ok(managerService.getManagerDashboardStats());
    }
}
