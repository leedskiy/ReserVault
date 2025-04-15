package io.leedsk1y.reservault_backend.config.cleanup;

import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import io.leedsk1y.reservault_backend.services.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CloudinaryCleanupConfig {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryCleanupConfig.class);
    private final HotelRepository hotelRepository;
    private final OfferRepository offerRepository;
    private final CloudinaryService cloudinaryService;

    @Value("${reservault.cleanup.enabled:false}")
    private boolean cleanupEnabled;

    public CloudinaryCleanupConfig(HotelRepository hotelRepository,
                                   OfferRepository offerRepository,
                                   CloudinaryService cloudinaryService) {
        this.hotelRepository = hotelRepository;
        this.offerRepository = offerRepository;
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Deletes all images from Cloudinary that are currently associated with hotels and offers.
     * This operation is only executed if the `reservault.cleanup.enabled` flag is set to `true`.
     * Used for development environment to reset state.
     */
    public void cleanupCloudinaryImages() {
        if (!cleanupEnabled) return;

        logger.info("Starting Cloudinary cleanup for hotel and offer images.");

        List<Hotel> hotels = hotelRepository.findAll();
        List<String> hotelsImageUrls = hotels.stream()
            .flatMap(hotel -> hotel.getImagesUrls().stream())
            .toList();

        List<Offer> offers = offerRepository.findAll();
        List<String> offersImageUrls = offers.stream()
                .flatMap(hotel -> hotel.getImagesUrls().stream())
                .toList();

        for (String imageUrl : hotelsImageUrls) {
            cloudinaryService.deleteImage(imageUrl, "hotels_images");
            logger.info("Deleted hotel image: {}", imageUrl);
        }

        for (String imageUrl : offersImageUrls) {
            cloudinaryService.deleteImage(imageUrl, "offers_images");
            logger.info("Deleted offer image: {}", imageUrl);
        }
    }
}
