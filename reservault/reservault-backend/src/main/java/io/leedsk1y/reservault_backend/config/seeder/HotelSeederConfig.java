package io.leedsk1y.reservault_backend.config.seeder;

import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.Location;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.services.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class HotelSeederConfig {
    private static final Logger logger = LoggerFactory.getLogger(HotelSeederConfig.class);
    private final HotelRepository hotelRepository;
    private final CloudinaryService cloudinaryService;

    public HotelSeederConfig(HotelRepository hotelRepository, CloudinaryService cloudinaryService) {
        this.hotelRepository = hotelRepository;
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Seeds predefined hotels into the database if none exist.
     * Each hotel is seeded with metadata and 3 associated image files,
     * which are uploaded to Cloudinary under the "hotels_images" folder.
     * @throws IOException If image files cannot be read or uploaded.
     */
    public void seedHotels() throws IOException {
        if (hotelRepository.count() > 0) {
            return;
        }

        logger.info("Seeding hotels...");

        List<Hotel> hotels = List.of(
                createHotel("mnhttnroyale",
                        "The Manhattan Royale",
                        "Nestled in the heart of Manhattan, The Manhattan Royale redefines luxury with its breathtaking skyline views, " +
                                "opulent interiors, and world-class amenities. Designed for those who appreciate sophistication, the hotel features " +
                                "grand chandeliers, plush suites with floor-to-ceiling windows, and an exclusive rooftop lounge offering panoramic " +
                                "views of the city that never sleeps. Whether indulging in gourmet dining, unwinding at the spa, or enjoying the " +
                                "vibrant nightlife just steps away, guests are treated to an unforgettable five-star experience in the heart of New York City.",
                        5, new Location("USA", "New York", "5th Avenue", "10001"),
                        List.of("hotel1_img1.png", "hotel1_img2.png", "hotel1_img3.png")),

                createHotel("azurepalms",
                        "Azure Palms Resort",
                        "Overlooking the stunning Mediterranean coastline, Azure Palms Resort is an oasis of relaxation and sophistication, " +
                                "where elegance meets tranquility in Barcelona. Surrounded by lush palm gardens and boasting direct access " +
                                "to pristine sandy beaches, this resort offers an unparalleled blend of modern luxury and coastal charm. " +
                                "Guests can enjoy rejuvenating spa treatments, world-class cuisine featuring fresh Mediterranean flavors, " +
                                "and infinity pools that seem to blend seamlessly with the sparkling blue sea. Whether seeking a romantic " +
                                "getaway or a serene retreat, Azure Palms Resort delivers a refined escape in one of Europe’s most captivating destinations.",
                        5, new Location("Spain", "Barcelona", "Beachfront Avenue", "08002"),
                        List.of("hotel2_img1.png", "hotel2_img2.png", "hotel2_img3.png"))
        );

        hotelRepository.saveAll(hotels);
    }

    /**
     * Creates a Hotel object with the specified properties and uploads associated images to Cloudinary.
     *
     * @param identifier Unique string identifier for the hotel.
     * @param name Display name of the hotel.
     * @param description Full hotel description.
     * @param stars Star rating of the hotel (e.g., 5).
     * @param location Location object including country, city, street, and zip.
     * @param imageFiles List of image file names to upload.
     * @return A fully constructed Hotel entity with uploaded image URLs.
     * @throws IOException If image upload fails.
     */
    private Hotel createHotel(String identifier, String name,
                              String description, int stars, Location location,
                              List<String> imageFiles) throws IOException {
        List<String> imageUrls = uploadImages(imageFiles);

        Hotel hotel = new Hotel();
        hotel.setIdentifier(identifier);
        hotel.setId(UUID.randomUUID());
        hotel.setName(name);
        hotel.setDescription(description);
        hotel.setStars(stars);
        hotel.setLocation(location);
        hotel.setImagesUrls(imageUrls);
        hotel.setCreatedAt(Instant.now());

        return hotel;
    }

    /**
     * Uploads a list of image files from the classpath to Cloudinary.
     * @param imageFiles List of file names located in `static/hotels-images/`.
     * @return List of secure image URLs returned from Cloudinary.
     * @throws IOException If any image file cannot be read or uploaded.
     */
    private List<String> uploadImages(List<String> imageFiles) throws IOException {
        return imageFiles.stream()
                .map(fileName -> {
                    try {
                        ClassPathResource resource = new ClassPathResource("static/hotels-images/" + fileName);

                        if (!resource.exists()) {
                            throw new RuntimeException("File not found: " + fileName);
                        }

                        File tempFile = File.createTempFile("upload_", "_" + fileName);
                        try (InputStream inputStream = resource.getInputStream()) {
                            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }

                        logger.info("Uploaded hotel image: {}", fileName);

                        return cloudinaryService.uploadImage(tempFile, "hotels_images");
                    } catch (IOException e) {
                        throw new RuntimeException("Error uploading image: " + fileName, e);
                    }
                })
                .collect(Collectors.toList());
    }
}