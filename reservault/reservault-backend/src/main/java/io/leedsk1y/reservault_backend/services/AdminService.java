package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.models.enums.EHotelManagerStatus;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private HotelRepository hotelRepository;
    private CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final HotelManagerRepository hotelManagerRepository;

    public AdminService(HotelRepository hotelRepository, CloudinaryService cloudinaryService,
                        UserRepository userRepository, HotelManagerRepository hotelManagerRepository) {
        this.hotelRepository = hotelRepository;
        this.cloudinaryService = cloudinaryService;
        this.userRepository = userRepository;
        this.hotelManagerRepository = hotelManagerRepository;
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Hotel createHotel(Hotel hotel, List<MultipartFile> images) throws IOException {
        if (hotelRepository.findByIdentifier(hotel.getIdentifier()).isPresent()) {
            throw new IllegalArgumentException("Hotel identifier already exists: " + hotel.getIdentifier());
        }

        for (MultipartFile image : images) {
            String imageUrl = cloudinaryService.uploadImage(image);
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
                        String imageUrl = cloudinaryService.uploadImage(image);
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
        Optional<Hotel> hotel = hotelRepository.findById(id);
        if (hotel.isPresent()) {
            Hotel h = hotel.get();

            for (String imageUrl : h.getImagesUrls()) {
                cloudinaryService.deleteImage(imageUrl);
            }

            hotelRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean removeHotelImage(UUID hotelId, String imageUrl) {
        Optional<Hotel> hotelOptional = hotelRepository.findById(hotelId);

        if (hotelOptional.isPresent()) {
            Hotel hotel = hotelOptional.get();

            if (hotel.getImagesUrls().contains(imageUrl)) {
                cloudinaryService.deleteImage(imageUrl);

                hotel.getImagesUrls().remove(imageUrl);
                hotelRepository.save(hotel);
                return true;
            }
        }
        return false;
    }

    public List<UserDetailedResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDetailedResponseDTO::new)
                .collect(Collectors.toList());
    }

    public Optional<UserDetailedResponseDTO> getUserById(UUID id) {

        return userRepository.findById(id)
                .map(UserDetailedResponseDTO::new);
    }

    public boolean deleteUser(UUID id) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        if (user.getRoles().contains("ROLE_ADMIN")) {
            throw new RuntimeException("Cannot delete an admin user.");
        }

        userRepository.deleteById(id);
        return true;
    }


    public boolean approveManagerRequest(UUID managerId) {
        Optional<User> userOptional = userRepository.findById(managerId);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        if (user.getRoles().contains("ROLE_MANAGER") && !user.isVerified()) {
            user.setVerified(true);
            userRepository.save(user);

            hotelManagerRepository.findByManagerId(managerId)
                    .forEach(hotelManager -> {
                        hotelManager.setStatus(EHotelManagerStatus.APPROVED);
                        hotelManagerRepository.save(hotelManager);
                    });

            return true;
        }

        return false;
    }

    public boolean rejectManagerRequest(UUID managerId) {
        if (!userRepository.existsById(managerId)) {
            return false;
        }

        hotelManagerRepository.deleteByManagerId(managerId);

        userRepository.deleteById(managerId);

        return true;
    }

    public List<HotelManager> getHotelsByManagerList(UUID managerId) {
        Optional<User> userOptional = userRepository.findById(managerId);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Manager not found.");
        }

        User manager = userOptional.get();

        if (!manager.getRoles().contains("ROLE_MANAGER")) {
            throw new RuntimeException("User is not a manager.");
        }

        return hotelManagerRepository.findByManagerId(managerId);
    }

    public List<HotelManager> updateHotelsByManagerList(UUID managerId, List<String> updatedHotelIdentifiers) {
        Optional<User> userOpt = userRepository.findById(managerId);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Manager not found.");
        }

        User manager = userOpt.get();

        if (!manager.getRoles().contains("ROLE_MANAGER")) {
            throw new IllegalArgumentException("User is not a manager.");
        }

        // 1. validate identifiers
        Set<String> validHotelIdentifiers = hotelRepository.findAll().stream()
                .map(Hotel::getIdentifier)
                .collect(Collectors.toSet());

        List<String> invalidIdentifiers = updatedHotelIdentifiers.stream()
                .filter(id -> !validHotelIdentifiers.contains(id))
                .collect(Collectors.toList());

        if (!invalidIdentifiers.isEmpty()) {
            throw new IllegalArgumentException("Invalid hotel identifiers: " + invalidIdentifiers);
        }

        // 2. get list of hotels managed by the manager
        List<HotelManager> existingHotelManagers = hotelManagerRepository.findByManagerId(managerId);
        Set<String> existingHotelIdentifiers = existingHotelManagers.stream()
                .map(HotelManager::getHotelIdentifier)
                .collect(Collectors.toSet());

        // 3. find the identifiers to remove
        List<String> toRemove = existingHotelIdentifiers.stream()
                .filter(hotelId -> !updatedHotelIdentifiers.contains(hotelId))
                .collect(Collectors.toList());

        // 4. find the identifiers to add
        List<String> toAdd = updatedHotelIdentifiers.stream()
                .filter(hotelId -> !existingHotelIdentifiers.contains(hotelId))
                .collect(Collectors.toList());

        // 5. Check if only one hotel relation remains before allowing deletion
        if (existingHotelManagers.size() - toRemove.size() + toAdd.size() <= 1) {
            throw new IllegalArgumentException("Manager must have at least one hotel-manager relation.");
        }

        // 6. remove old hotel-manager associations
        hotelManagerRepository.deleteByManagerIdAndHotelIdentifierIn(managerId, toRemove);

        // 7. add new hotel-manager associations
        for (String hotelIdentifier : toAdd) {
            HotelManager hotelManager = new HotelManager(hotelIdentifier, managerId);
            hotelManager.setStatus(manager.isVerified() ? EHotelManagerStatus.APPROVED : EHotelManagerStatus.PENDING);
            hotelManagerRepository.save(hotelManager);
        }

        // 8. return updated hotels
        return hotelManagerRepository.findByManagerId(managerId);
    }

}