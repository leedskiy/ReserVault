package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.models.entities.Booking;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.repositories.BookingRepository;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserDeletionService {
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final OfferRepository offerRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final ReviewService reviewService;
    private final CloudinaryService cloudinaryService;
    private final OfferService offerService;

    public UserDeletionService(UserRepository userRepository,
                               BookingService bookingService,
                               BookingRepository bookingRepository,
                               OfferRepository offerRepository,
                               HotelManagerRepository hotelManagerRepository,
                               ReviewService reviewService,
                               CloudinaryService cloudinaryService,
                               OfferService offerService) {
        this.userRepository = userRepository;
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.offerRepository = offerRepository;
        this.hotelManagerRepository = hotelManagerRepository;
        this.reviewService = reviewService;
        this.cloudinaryService = cloudinaryService;
        this.offerService = offerService;
    }

    public void deleteUser(UUID userId) {
        // delete bookings, payments, booked dates, reviews
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        for (Booking booking : bookings) {
            bookingService.deleteBooking(booking.getId());
            reviewService.deleteReviewFromOffer(booking.getOfferId(), booking.getId());
        }

        // delete user
        userRepository.deleteById(userId);
    }

    public void deleteManager(UUID managerId) {
        List<HotelManager> hotelManagers = hotelManagerRepository.findByManagerId(managerId);

        for (HotelManager hotelManager : hotelManagers) {
            List<Offer> offers = offerRepository.findByHotelIdentifier(hotelManager.getHotelIdentifier());

            for (Offer offer : offers) {
                offerService.deleteOffer(offer.getId(), managerId);
            }

            hotelManagerRepository.deleteById(hotelManager.getId());
        }

        userRepository.deleteById(managerId);
    }
}
