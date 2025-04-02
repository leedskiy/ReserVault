package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HotelService {
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

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Hotel createHotel(Hotel hotel, List<MultipartFile> images) throws IOException {
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

    public Optional<Hotel> updateHotel(UUID id, Hotel updatedHotel, List<MultipartFile> newImages) throws IOException {
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

    public boolean deleteHotel(UUID id) {
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

    public boolean removeHotelImage(UUID hotelId, String imageUrl) {
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

    public Optional<Hotel> getHotelByIdentifier(String identifier) {
        return hotelRepository.findByIdentifier(identifier);
    }
}
