package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.AdminDashboardStatsDTO;
import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.models.enums.EHotelManagerStatus;
import io.leedsk1y.reservault_backend.repositories.BookingRepository;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final HotelService hotelService;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final UserDeletionService userDeletionService;
    private final OfferRepository offerRepository;
    private final BookingRepository bookingRepository;

    public AdminService(HotelService hotelService,
                        HotelRepository hotelRepository,
                        UserRepository userRepository,
                        HotelManagerRepository hotelManagerRepository,
                        UserDeletionService userDeletionService,
                        OfferRepository offerRepository,
                        BookingRepository bookingRepository) {
        this.hotelService = hotelService;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.hotelManagerRepository = hotelManagerRepository;
        this.userDeletionService = userDeletionService;
        this.offerRepository = offerRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Retrieves all hotels via the HotelService.
     * @return A list of Hotel entities.
     */
    public List<Hotel> getAllHotels() {
        logger.info("Fetching all hotels via AdminService");
        return hotelService.getAllHotels();
    }

    /**
     * Creates a new hotel with the provided data and images.
     * @param hotel The Hotel entity to be created.
     * @param images List of associated images for the hotel.
     * @return The created Hotel entity.
     * @throws IOException If image processing fails.
     */
    public Hotel createHotel(Hotel hotel, List<MultipartFile> images) throws IOException {
        logger.info("Creating hotel: {}", hotel.getName());
        return hotelService.createHotel(hotel, images);
    }

    /**
     * Updates a hotel with the given ID using new data and images.
     * @param id UUID of the hotel to be updated.
     * @param updatedHotel Updated Hotel entity.
     * @param newImages Optional new images for the hotel.
     * @return Optional containing the updated Hotel or empty if not found.
     * @throws IOException If image processing fails.
     */
    public Optional<Hotel> updateHotel(UUID id, Hotel updatedHotel, List<MultipartFile> newImages) throws IOException {
        logger.info("Updating hotel with ID: {}", id);
        return hotelService.updateHotel(id, updatedHotel, newImages);
    }

    /**
     * Deletes a hotel by its UUID.
     * @param id UUID of the hotel to delete.
     * @return True if deletion was successful, false otherwise.
     */
    public boolean deleteHotel(UUID id) {
        logger.info("Deleting hotel with ID: {}", id);
        return hotelService.deleteHotel(id);
    }

    /**
     * Removes an image from a specific hotel.
     * @param hotelId UUID of the hotel.
     * @param imageUrl URL of the image to be removed.
     * @return True if removal succeeded, false otherwise.
     */
    public boolean removeHotelImage(UUID hotelId, String imageUrl) {
        logger.info("Removing image from hotel ID: {}, Image URL: {}", hotelId, imageUrl);
        return hotelService.removeHotelImage(hotelId, imageUrl);
    }

    /**
     * Retrieves all users with detailed information.
     * @return A list of UserDetailedResponseDTO.
     */
    public List<UserDetailedResponseDTO> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(UserDetailedResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Fetches a single user's detailed info by their UUID.
     * @param id UUID of the user.
     * @return Optional containing UserDetailedResponseDTO or empty if not found.
     */
    public Optional<UserDetailedResponseDTO> getUserById(UUID id) {
        logger.info("Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .map(UserDetailedResponseDTO::new);
    }

    /**
     * Deletes a user by UUID based on their role.
     * @param userId UUID of the user to delete.
     * @throws ResponseStatusException If the user is not found or has an invalid role.
     */
    public void deleteUser(UUID userId) {
        logger.info("Deleting user with ID: {}", userId);
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

    /**
     * Approves a manager's request and updates their verification and hotel-manager statuses.
     * @param managerId UUID of the manager.
     * @return True if any changes were made, false otherwise.
     */
    public boolean approveManagerRequest(UUID managerId) {
        logger.info("Approving manager request for ID: {}", managerId);
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

    /**
     * Rejects a manager request by removing all their records.
     * @param managerId UUID of the manager to reject.
     * @return True if the manager was found and deleted, false otherwise.
     */
    public boolean rejectManagerRequest(UUID managerId) {
        logger.info("Rejecting and deleting manager with ID: {}", managerId);
        if (!userRepository.existsById(managerId)) {
            return false;
        }

        hotelManagerRepository.deleteByManagerId(managerId);

        userRepository.deleteById(managerId);

        return true;
    }

    /**
     * Retrieves all hotel-manager relationships for a given manager.
     * @param managerId UUID of the manager.
     * @return A list of HotelManager entities.
     * @throws RuntimeException If the manager is not found or not valid.
     */
    public List<HotelManager> getHotelsByManagerList(UUID managerId) {
        logger.info("Fetching hotels managed by manager ID: {}", managerId);
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

    /**
     * Updates the list of hotels associated with a manager.
     * @param managerId UUID of the manager.
     * @param updatedHotelIdentifiers New list of hotel identifiers to associate.
     * @return Updated list of HotelManager entities.
     * @throws IllegalArgumentException If manager is not found, invalid, or hotel list is invalid.
     */
    public List<HotelManager> updateHotelsByManagerList(UUID managerId, List<String> updatedHotelIdentifiers) {
        logger.info("Updating hotel-manager relations for manager ID: {}", managerId);
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


    /**
     * Generates and retrieves statistics for the admin dashboard.
     * @return AdminDashboardStatsDTO containing platform statistics.
     */
    public AdminDashboardStatsDTO getAdminDashboardStats() {
        logger.info("Generating admin dashboard statistics");
        long totalUsers = userRepository.count();

        long verifiedManagers = userRepository
                .findAll().stream()
                .filter(user -> user.getRoles().contains("ROLE_MANAGER") && user.isVerified())
                .count();

        long totalManagers = userRepository
                .findAll().stream()
                .filter(user -> user.getRoles().contains("ROLE_MANAGER"))
                .count();

        long totalHotels = hotelRepository.count();
        long totalOffers = offerRepository.count();
        long totalBookings = bookingRepository.count();

        return new AdminDashboardStatsDTO(
                totalUsers,
                verifiedManagers,
                totalHotels,
                totalOffers,
                totalManagers,
                totalBookings
        );
    }
}