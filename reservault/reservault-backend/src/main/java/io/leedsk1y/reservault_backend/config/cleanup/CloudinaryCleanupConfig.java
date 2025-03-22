package io.leedsk1y.reservault_backend.config.cleanup;

import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import io.leedsk1y.reservault_backend.services.CloudinaryService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CloudinaryCleanupConfig {
    private final HotelRepository hotelRepository;
    private final OfferRepository offerRepository;
    private final CloudinaryService cloudinaryService;

    public CloudinaryCleanupConfig(HotelRepository hotelRepository,
                                   OfferRepository offerRepository,
                                   CloudinaryService cloudinaryService) {
        this.hotelRepository = hotelRepository;
        this.offerRepository = offerRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public void cleanupCloudinaryImages() {
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
        }

        for (String imageUrl : offersImageUrls) {
            cloudinaryService.deleteImage(imageUrl, "offers_images");
        }
    }
}
