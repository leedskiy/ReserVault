package io.leedsk1y.reservault_backend.config.seeder;


import io.leedsk1y.reservault_backend.models.entities.Facilities;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.models.enums.EHotelManagerStatus;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import io.leedsk1y.reservault_backend.services.CloudinaryService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class OfferSeederConfig {
    private final OfferRepository offerRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final CloudinaryService cloudinaryService;

    public OfferSeederConfig(OfferRepository offerRepository,
                             HotelRepository hotelRepository,
                             UserRepository userRepository,
                             HotelManagerRepository hotelManagerRepository,
                             CloudinaryService cloudinaryService) {
        this.offerRepository = offerRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.hotelManagerRepository = hotelManagerRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Bean
    public ApplicationRunner runOfferSeeder() {
        return args -> {
            if (offerRepository.count() > 0) return;

            String managerEmail = "manager@example.moc";
            User manager = waitForManager(managerEmail, 5, 2000);

            String hotelIdentifier = "azurepalms";
            Hotel hotel = waitForHotel(hotelIdentifier, 5, 2000);

            HotelManager hotelManager = hotelManagerRepository
                    .findByManagerIdAndHotelIdentifier(manager.getId(), hotelIdentifier)
                    .orElseGet(() -> {
                        HotelManager newManager = new HotelManager(hotelIdentifier, manager.getId(), EHotelManagerStatus.APPROVED);
                        return hotelManagerRepository.save(newManager);
                    });

            createOffer(
                    manager,
                    hotel,
                    hotelManager,
                    "04.10.2025",
                    "04.15.2025",
                    3,
                    6,
                    new BigDecimal("179.99"),
                    new Facilities(true, true, true, true, true),
                    List.of("hotel2_offer1_img1.png", "hotel2_offer1_img2.png")
            );

            createOffer(
                    manager,
                    hotel,
                    hotelManager,
                    "05.01.2025",
                    "05.07.2025",
                    2,
                    4,
                    new BigDecimal("139.50"),
                    new Facilities(true, false, true, false, false),
                    List.of("hotel2_offer2_img1.png", "hotel2_offer2_img2.png")
            );
        };
    }

    private void createOffer(User manager,
                             Hotel hotel,
                             HotelManager hotelManager,
                             String dateFrom,
                             String dateUntil,
                             int roomCount,
                             int peopleCount,
                             BigDecimal pricePerNight,
                             Facilities facilities,
                             List<String> imageFiles) throws IOException {

        List<String> imageUrls = uploadImages(imageFiles);

        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setHotelIdentifier(hotel.getIdentifier());
        offer.setManagerId(manager.getId());
        offer.setHotelManagerId(hotelManager.getId());
        offer.setDateFrom(dateFrom);
        offer.setDateUntil(dateUntil);
        offer.setRoomCount(roomCount);
        offer.setPeopleCount(peopleCount);
        offer.setPricePerNight(pricePerNight);
        offer.setFacilities(facilities);
        offer.setImagesUrls(imageUrls);
        offer.setCreatedAt(Instant.now());
        offer.setReviews(new ArrayList<>());

        offerRepository.save(offer);
    }

    private List<String> uploadImages(List<String> imageFiles) throws IOException {
        return imageFiles.stream()
                .map(fileName -> {
                    try {
                        ClassPathResource resource = new ClassPathResource("static/offers-images/" + fileName);

                        if (!resource.exists()) {
                            throw new RuntimeException("File not found: " + fileName);
                        }

                        File tempFile = File.createTempFile("upload_", "_" + fileName);
                        try (InputStream inputStream = resource.getInputStream()) {
                            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }

                        return cloudinaryService.uploadImage(tempFile, "offers_images");
                    } catch (IOException e) {
                        throw new RuntimeException("Error uploading image: " + fileName, e);
                    }
                })
                .collect(Collectors.toList());
    }

    private User waitForManager(String email, int maxAttempts, long delayMillis) {
        for (int i = 0; i < maxAttempts; i++) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                System.out.println("[OfferSeeder] Manager loaded after " + (i + 1) + " attempt(s).");
                return userOpt.get();
            }

            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new RuntimeException("Manager user not found after waiting: " + email);
    }

    private Hotel waitForHotel(String identifier, int maxAttempts, long delayMillis) {
        for (int i = 0; i < maxAttempts; i++) {
            Optional<Hotel> hotelOpt = hotelRepository.findByIdentifier(identifier);
            if (hotelOpt.isPresent()) {
                System.out.println("[OfferSeeder] Hotel loaded after " + (i + 1) + " attempt(s).");
                return hotelOpt.get();
            }

            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new RuntimeException("Hotel not found after waiting: " + identifier);
    }
}