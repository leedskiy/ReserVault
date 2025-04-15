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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class OfferSeederConfig {
    private static final Logger logger = LoggerFactory.getLogger(OfferSeederConfig.class);
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

    /**
     * Seeds the database with predefined offers for the "Azure Palms Resort" hotel.
     * @throws IOException If image upload fails during offer creation
     */
    public void seedOffers() throws IOException {
            if (offerRepository.count() > 0) return;

            logger.info("Seeding offers...");

            User manager = userRepository.findByEmail("manager@example.moc")
                    .orElseThrow(() -> new RuntimeException("Manager user not found"));

            Hotel hotel = hotelRepository.findByIdentifier("azurepalms")
                    .orElseThrow(() -> new RuntimeException("Hotel not found"));

            HotelManager hotelManager = hotelManagerRepository
                    .findByManagerIdAndHotelIdentifier(manager.getId(), hotel.getIdentifier())
                    .orElseGet(() -> hotelManagerRepository.save(
                            new HotelManager(hotel.getIdentifier(), manager.getId(), EHotelManagerStatus.APPROVED)
                    ));

            createOffer(
                    manager,
                    hotel,
                    hotelManager,
                    "Spring Serenity Escape",
                    "Experience the refreshing beauty of Barcelona in full bloom with our Spring Serenity Escape. " +
                            "Nestled within the peaceful grounds of Azure Palms Resort, this 5-night stay offers spacious " +
                            "accommodations for up to 6 guests across 3 serene rooms. Wake up to the sound of ocean waves, " +
                            "unwind in our infinity pool, and explore vibrant city life just moments away. Whether you're " +
                            "traveling with friends or family, this escape blends coastal relaxation with cultural discovery.",
                    10,
                    "02.07.2027",
                    "02.14.2027",
                    3,
                    6,
                    new BigDecimal("179.99"),
                    new Facilities(true, false, true, false, true),
                    List.of("hotel2_offer1_img1.png", "hotel2_offer1_img2.png")
            );

            createOffer(
                    manager,
                    hotel,
                    hotelManager,
                    "Mediterranean Bliss Getaway",
                    "Soak up the early summer sun with our Mediterranean Bliss Getaway at the stunning Azure Palms " +
                            "Resort. Ideal for couples or small groups, this 6-night package invites you to indulge in gourmet " +
                            "cuisine, rejuvenating spa treatments, and breathtaking sea views. With luxurious amenities and " +
                            "direct beach access, it's the perfect way to recharge and celebrate the start of the season in style.",
                    10,
                    "02.20.2027",
                    "02.28.2027",
                    2,
                    4,
                    new BigDecimal("139.50"),
                    new Facilities(true, false, true, false, false),
                    List.of("hotel2_offer2_img1.png", "hotel2_offer2_img2.png")
            );
    }

    /**
     * Creates and saves a single offer with specified metadata and images.
     * @param manager The manager responsible for the offer
     * @param hotel The hotel the offer belongs to
     * @param hotelManager The existing or newly created hotel-manager relationship
     * @param title The offer's title
     * @param description The offer's description
     * @param rating Initial average rating of the offer
     * @param dateFrom Offer start date (MM.dd.yyyy)
     * @param dateUntil Offer end date (MM.dd.yyyy)
     * @param roomCount Number of rooms in the offer
     * @param peopleCount Max number of people supported
     * @param pricePerNight Price per night in BigDecimal
     * @param facilities Facilities included in the offer
     * @param imageFiles List of image filenames (resources)
     * @throws IOException If image upload fails
     */
    private void createOffer(User manager,
                             Hotel hotel,
                             HotelManager hotelManager,
                             String title,
                             String description,
                             double rating,
                             String dateFrom,
                             String dateUntil,
                             int roomCount,
                             int peopleCount,
                             BigDecimal pricePerNight,
                             Facilities facilities,
                             List<String> imageFiles) throws IOException {
        logger.info("Creating offer: {}", title);

        List<String> imageUrls = uploadImages(imageFiles);

        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setHotelIdentifier(hotel.getIdentifier());
        offer.setManagerId(manager.getId());
        offer.setHotelManagerId(hotelManager.getId());
        offer.setTitle(title);
        offer.setDescription(description);
        offer.setRating(rating);
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

    /**
     * Uploads images from the classpath (resources/static/offers-images) to Cloudinary.
     * @param imageFiles List of image file names to upload
     * @return List of uploaded image URLs from Cloudinary
     * @throws IOException If file reading or upload fails
     */
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

                        logger.info("Uploaded offer image: {}", fileName);

                        return cloudinaryService.uploadImage(tempFile, "offers_images");
                    } catch (IOException e) {
                        throw new RuntimeException("Error uploading image: " + fileName, e);
                    }
                })
                .collect(Collectors.toList());
    }
}