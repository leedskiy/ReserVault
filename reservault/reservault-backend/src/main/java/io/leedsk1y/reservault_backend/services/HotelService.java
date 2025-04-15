package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HotelService {
    private static final Logger logger = LoggerFactory.getLogger(HotelService.class);
    private final HotelRepository hotelRepository;
    private final CloudinaryService cloudinaryService;
    private final OfferRepository offerRepository;
    private final OfferService offerService;
    private final HotelManagerRepository hotelManagerRepository;

    public HotelService(HotelRepository hotelRepository,
                        CloudinaryService cloudinaryService,
                        OfferRepository offerRepository,
                        OfferService offerService,
                        HotelManagerRepository hotelManagerRepository) {
        this.hotelRepository = hotelRepository;
        this.cloudinaryService = cloudinaryService;
        this.offerRepository = offerRepository;
        this.offerService = offerService;
        this.hotelManagerRepository = hotelManagerRepository;
    }

    /**
     * Retrieves all hotels from the repository.
     * @return A list of all Hotel entities.
     */
    public List<Hotel> getAllHotels() {
        logger.info("Fetching all hotels");
        return hotelRepository.findAll();
    }

    /**
     * Creates a new hotel and uploads its associated images to Cloudinary.
     * @param hotel The hotel entity to create.
     * @param images A list of images to upload and associate with the hotel.
     * @return The saved Hotel entity.
     * @throws IOException If image upload fails.
     */
    public Hotel createHotel(Hotel hotel, List<MultipartFile> images) throws IOException {
        logger.info("Creating hotel with identifier: {}", hotel.getIdentifier());
        if (hotelRepository.findByIdentifier(hotel.getIdentifier()).isPresent()) {
            throw new IllegalArgumentException("Hotel identifier already exists: " + hotel.getIdentifier());
        }

        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("At least one image is required to create a hotel.");
        }

        for (MultipartFile image : images) {
            String imageUrl = cloudinaryService.uploadImage(image, "hotels_images");
            hotel.getImagesUrls().add(imageUrl);
        }

        hotel.setId(UUID.randomUUID());
        hotel.setCreatedAt(Instant.now());
        return hotelRepository.save(hotel);
    }

    /**
     * Updates an existing hotel with new details and optionally new images.
     * @param id UUID of the hotel to update.
     * @param updatedHotel The updated hotel information.
     * @param newImages Optional list of new images to add.
     * @return Optional containing the updated Hotel or empty if not found.
     */
    public Optional<Hotel> updateHotel(UUID id, Hotel updatedHotel, List<MultipartFile> newImages) {
        logger.info("Updating hotel with ID: {}", id);
        return hotelRepository.findById(id).map(existingHotel -> {
            try {
                if (updatedHotel.getImagesUrls() != null && !updatedHotel.getImagesUrls().isEmpty()) {
                    existingHotel.setImagesUrls(updatedHotel.getImagesUrls());
                }

                if (newImages != null && !newImages.isEmpty()) {
                    for (MultipartFile image : newImages) {
                        String imageUrl = cloudinaryService.uploadImage(image, "hotels_images");
                        existingHotel.getImagesUrls().add(imageUrl);
                    }
                }

                existingHotel.setName(updatedHotel.getName());
                existingHotel.setDescription(updatedHotel.getDescription());
                existingHotel.setStars(updatedHotel.getStars());
                existingHotel.setLocation(updatedHotel.getLocation());

                return hotelRepository.save(existingHotel);
            } catch (IOException e) {
                throw new RuntimeException("Error uploading images: " + e.getMessage());
            }
        });
    }

    /**
     * Deletes a hotel by its ID, including related hotel-manager relations, offers, and images.
     * @param id UUID of the hotel to delete.
     * @return True if deletion was successful, false if hotel not found.
     */
    public boolean deleteHotel(UUID id) {
        logger.info("Deleting hotel with ID: {}", id);
        Optional<Hotel> hotelOptional = hotelRepository.findById(id);
        if (hotelOptional.isEmpty()) {
            return false;
        }

        Hotel hotel = hotelOptional.get();
        String hotelIdentifier = hotel.getIdentifier();

        // 1. delete hotelmanager
        hotelManagerRepository.deleteByHotelIdentifier(hotelIdentifier);

        // 2. delete offers
        List<Offer> offers = offerRepository.findByHotelIdentifier(hotelIdentifier);
        for (Offer offer : offers) {
            offerService.deleteOffer(offer.getId(), null);
        }

        // 3. delete hotel images
        for (String imageUrl : hotel.getImagesUrls()) {
            cloudinaryService.deleteImage(imageUrl, "hotels_images");
        }

        // 4. delete hotel
        hotelRepository.deleteById(id);

        return true;
    }

    /**
     * Removes a specific image from a hotel by ID and image URL.
     * @param hotelId UUID of the hotel.
     * @param imageUrl URL of the image to remove.
     * @return True if the image was removed successfully, false otherwise.
     */
    public boolean removeHotelImage(UUID hotelId, String imageUrl) {
        logger.info("Removing image from hotel ID: {}, Image URL: {}", hotelId, imageUrl);
        Optional<Hotel> hotelOptional = hotelRepository.findById(hotelId);

        if (hotelOptional.isPresent()) {
            Hotel hotel = hotelOptional.get();

            if (hotel.getImagesUrls().contains(imageUrl)) {
                cloudinaryService.deleteImage(imageUrl, "hotels_images");

                hotel.getImagesUrls().remove(imageUrl);
                hotelRepository.save(hotel);
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves a hotel using its unique identifier.
     * @param identifier The unique identifier of the hotel.
     * @return Optional containing the Hotel or empty if not found.
     */
    public Optional<Hotel> getHotelByIdentifier(String identifier) {
        logger.info("Fetching hotel by identifier: {}", identifier);
        return hotelRepository.findByIdentifier(identifier);
    }
}
