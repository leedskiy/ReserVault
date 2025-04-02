package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.BookedDates;
import io.leedsk1y.reservault_backend.models.entities.Booking;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.models.enums.EHotelManagerStatus;
import io.leedsk1y.reservault_backend.repositories.BookedDatesRepository;
import io.leedsk1y.reservault_backend.repositories.BookingRepository;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import io.leedsk1y.reservault_backend.repositories.PaymentRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final HotelService hotelService;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final UserDeletionService userDeletionService;

    public AdminService(HotelService hotelService,
                        HotelRepository hotelRepository,
                        UserRepository userRepository,
                        HotelManagerRepository hotelManagerRepository,
                        UserDeletionService userDeletionService) {
        this.hotelService = hotelService;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.hotelManagerRepository = hotelManagerRepository;
        this.userDeletionService = userDeletionService;
    }

    public List<Hotel> getAllHotels() {
        return hotelService.getAllHotels();
    }

    public Hotel createHotel(Hotel hotel, List<MultipartFile> images) throws IOException {
        return hotelService.createHotel(hotel, images);
    }

    public Optional<Hotel> updateHotel(UUID id, Hotel updatedHotel, List<MultipartFile> newImages) throws IOException {
        return hotelService.updateHotel(id, updatedHotel, newImages);
    }

    public boolean deleteHotel(UUID id) {
        return hotelService.deleteHotel(id);
    }

    public boolean removeHotelImage(UUID hotelId, String imageUrl) {
        return hotelService.removeHotelImage(hotelId, imageUrl);
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

    public void deleteUser(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User user = userOpt.get();

        if (user.getRoles().contains("ROLE_MANAGER")) {
            userDeletionService.deleteManager(userId);
        } else if (user.getRoles().contains("ROLE_USER")) {
            userDeletionService.deleteUser(userId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user role for deletion");
        }
    }

    public boolean approveManagerRequest(UUID managerId) {
        Optional<User> userOptional = userRepository.findById(managerId);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        if (!user.getRoles().contains("ROLE_MANAGER")) {
            return false;
        }

        boolean isUpdated = false;

        if (!user.isVerified()) {
            user.setVerified(true);
            userRepository.save(user);
            isUpdated = true;
        }

        List<HotelManager> pendingRelations = hotelManagerRepository.findByManagerId(managerId).stream()
                .filter(hm -> hm.getStatus() == EHotelManagerStatus.PENDING)
                .collect(Collectors.toList());

        if (!pendingRelations.isEmpty()) {
            pendingRelations.forEach(hm -> {
                hm.setStatus(EHotelManagerStatus.APPROVED);
                hotelManagerRepository.save(hm);
            });
            isUpdated = true;
        }

        return isUpdated;
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
        if (existingHotelManagers.size() - toRemove.size() + toAdd.size() < 1) {
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