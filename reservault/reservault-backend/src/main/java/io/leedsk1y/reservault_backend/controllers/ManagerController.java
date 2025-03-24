package io.leedsk1y.reservault_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leedsk1y.reservault_backend.dto.OfferWithLocationDTO;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.services.ManagerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @GetMapping("/offers")
    public ResponseEntity<List<OfferWithLocationDTO>> getManagerOffers() {
        return ResponseEntity.ok(managerService.getManagerOffers());
    }

    @GetMapping("/hotels")
    public ResponseEntity<?> getHotelsByManagerList() {
        try {
            List<HotelManager> hotelManagers = managerService.getHotelsByManagerList();
            return ResponseEntity.ok(hotelManagers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value="/offers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createOffer(
            @RequestPart("offer") String offerJson,
            @RequestPart(value = "images") List<MultipartFile> images) {
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

    @PutMapping(value = "/offers/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateOffer(
            @PathVariable UUID id,
            @RequestPart("offer") String offerJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOffer(@PathVariable UUID id) {
        managerService.deleteOffer(id);
        return ResponseEntity.ok().body("Offer deleted successfully");
    }

    @DeleteMapping("/offers/{offerId}/images")
    public ResponseEntity<String> removeOfferImage(
            @PathVariable UUID offerId,
            @RequestParam("imageUrl") String imageUrl) {
        boolean removed = managerService.removeOfferImage(offerId, imageUrl);
        return removed
                ? ResponseEntity.ok("Image removed successfully")
                : ResponseEntity.notFound().build();
    }
}
