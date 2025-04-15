package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.models.entities.Booking;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.repositories.BookingRepository;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserDeletionService {
    private static final Logger logger = LoggerFactory.getLogger(UserDeletionService.class);
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final OfferRepository offerRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final OfferService offerService;

    public UserDeletionService(UserRepository userRepository,
                               BookingService bookingService,
                               BookingRepository bookingRepository,
                               OfferRepository offerRepository,
                               HotelManagerRepository hotelManagerRepository,
                               OfferService offerService) {
        this.userRepository = userRepository;
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.offerRepository = offerRepository;
        this.hotelManagerRepository = hotelManagerRepository;
        this.offerService = offerService;
    }

    /**
     * Deletes a user and all their associated bookings.
     * @param userId UUID of the user to delete.
     */
    public void deleteUser(UUID userId) {
        logger.info("Deleting user with ID: {}", userId);
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        for (Booking booking : bookings) {
            bookingService.deleteBooking(booking.getId());
        }

        userRepository.deleteById(userId);
    }

    /**
     * Deletes a manager, their hotel-manager associations, and all offers they created.
     * Also deletes any related bookings tied to those offers.
     * @param managerId UUID of the manager to delete.
     */
    public void deleteManager(UUID managerId) {
        logger.info("Deleting manager with ID: {}", managerId);
        List<HotelManager> hotelManagers = hotelManagerRepository.findByManagerId(managerId);

        for (HotelManager hotelManager : hotelManagers) {
            List<Offer> offers = offerRepository.findByHotelIdentifierAndManagerId(hotelManager.getHotelIdentifier(), managerId);

            for (Offer offer : offers) {
                offerService.deleteOffer(offer.getId(), managerId);
            }

            hotelManagerRepository.deleteById(hotelManager.getId());
        }

        userRepository.deleteById(managerId);
    }
}
