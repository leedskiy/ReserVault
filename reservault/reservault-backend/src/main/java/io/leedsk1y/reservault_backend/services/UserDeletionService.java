package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.models.entities.BookedDates;
import io.leedsk1y.reservault_backend.models.entities.Booking;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.repositories.BookedDatesRepository;
import io.leedsk1y.reservault_backend.repositories.BookingRepository;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import io.leedsk1y.reservault_backend.repositories.PaymentRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserDeletionService {

    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final BookedDatesRepository bookedDatesRepository;
    private final OfferRepository offerRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final ReviewService reviewService;
    private final CloudinaryService cloudinaryService;

    public UserDeletionService(UserRepository userRepository,
                               BookingService bookingService,
                               BookingRepository bookingRepository,
                               PaymentRepository paymentRepository,
                               BookedDatesRepository bookedDatesRepository,
                               OfferRepository offerRepository,
                               HotelManagerRepository hotelManagerRepository,
                               ReviewService reviewService,
                               CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.bookedDatesRepository = bookedDatesRepository;
        this.offerRepository = offerRepository;
        this.hotelManagerRepository = hotelManagerRepository;
        this.reviewService = reviewService;
        this.cloudinaryService = cloudinaryService;
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
        // delete hotel-manager relationships
        List<HotelManager> hotelManagers = hotelManagerRepository.findByManagerId(managerId);

        for (HotelManager hotelManager : hotelManagers) {
            List<Offer> offers = offerRepository.findByHotelIdentifier(hotelManager.getHotelIdentifier());

            for (Offer offer : offers) {
                // delete bookings, payments, booked dates, offer images
                List<Booking> bookings = bookingRepository.findByOfferId(offer.getId());
                for (Booking booking : bookings) {
                    bookingService.deleteBooking(booking.getId());
                }

                // delete offer images
                for (String imageUrl : offer.getImagesUrls()) {
                    cloudinaryService.deleteImage(imageUrl, "offers_images");
                }

                // delete offer
                offerRepository.deleteById(offer.getId());
            }

            // delete hotel-manager
            hotelManagerRepository.deleteById(hotelManager.getId());
        }

        // delete manager user account
        userRepository.deleteById(managerId);
    }
}
