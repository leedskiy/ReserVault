package io.leedsk1y.reservault_backend.config.cleanup;

import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.services.CloudinaryService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CloudinaryCleanupConfig {
    private final HotelRepository hotelRepository;
    private final CloudinaryService cloudinaryService;

    public CloudinaryCleanupConfig(HotelRepository hotelRepository, CloudinaryService cloudinaryService) {
        this.hotelRepository = hotelRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public void cleanupCloudinaryImages() {
        List<Hotel> hotels = hotelRepository.findAll();
        List<String> imageUrls = hotels.stream()
            .flatMap(hotel -> hotel.getImagesUrls().stream())
            .toList();

        for (String imageUrl : imageUrls) {
            cloudinaryService.deleteImage(imageUrl);
        }
    }
}
