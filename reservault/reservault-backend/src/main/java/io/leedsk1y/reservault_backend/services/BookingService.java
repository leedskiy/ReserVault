package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.BookingResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.BookedDates;
import io.leedsk1y.reservault_backend.models.entities.Booking;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.Location;
import io.leedsk1y.reservault_backend.models.entities.Offer;
import io.leedsk1y.reservault_backend.models.entities.Payment;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.models.enums.EBookingStatus;
import io.leedsk1y.reservault_backend.models.enums.EPaymentStatus;
import io.leedsk1y.reservault_backend.repositories.BookedDatesRepository;
import io.leedsk1y.reservault_backend.repositories.BookingRepository;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.OfferRepository;
import io.leedsk1y.reservault_backend.repositories.PaymentRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final BookedDatesRepository bookedDatesRepository;
    private final HotelRepository hotelRepository;
    private final PaymentRepository paymentRepository;

    public BookingService(BookingRepository bookingRepository,
                          OfferRepository offerRepository,
                          UserRepository userRepository,
                          BookedDatesRepository bookedDatesRepository,
                          HotelRepository hotelRepository,
                          PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.bookedDatesRepository = bookedDatesRepository;
        this.hotelRepository = hotelRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Creates a new booking for an offer after validating date availability and range.
     * Also initializes a pending payment and records booked dates.
     * @param booking The booking entity to be created.
     * @return The saved booking with payment and booking IDs set.
     */
    public Booking createBooking(Booking booking) {
        logger.info("Creating booking for offerId: {}", booking.getOfferId());
        User user = getAuthenticatedUser();

        Offer offer = offerRepository.findById(booking.getOfferId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        booking.setId(UUID.randomUUID());
        booking.setUserId(user.getId());
        booking.setStatus(EBookingStatus.PENDING);
        booking.setCreatedAt(Instant.now());
        booking.setExpiresAt(booking.getCreatedAt().plusSeconds(3600)); // 1h

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd.yyyy");

        LocalDate newStart = LocalDate.parse(booking.getDateFrom(), formatter);
        LocalDate newEnd = LocalDate.parse(booking.getDateUntil(), formatter);

        LocalDate today = LocalDate.now();
        if (newStart.isBefore(today) || newEnd.isBefore(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking dates cannot be in the past");
        }

        if (!newStart.isBefore(newEnd) && !newStart.equals(newEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking dates");
        }

        LocalDate offerStart = LocalDate.parse(offer.getDateFrom(), formatter);
        LocalDate offerEnd = LocalDate.parse(offer.getDateUntil(), formatter);

        if (newStart.isBefore(offerStart) || newEnd.isAfter(offerEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Booking must be within offer's availability range (" + offer.getDateFrom() + " to " + offer.getDateUntil() + ")");
        }

        List<BookedDates> existingBookings = bookedDatesRepository.findByOfferId(booking.getOfferId());
        for (BookedDates existing : existingBookings) {
            if (existing.getDateFrom() == null || existing.getDateUntil() == null) continue;

            LocalDate existingStart = LocalDate.parse(existing.getDateFrom(), formatter);
            LocalDate existingEnd = LocalDate.parse(existing.getDateUntil(), formatter);

            if (!(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected dates are already booked");
            }
        }

        BigDecimal totalPrice = calculateTotalPrice(newStart, newEnd, offer.getPricePerNight());
        booking.setPrice(totalPrice);

        bookingRepository.save(booking);

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setBookingId(booking.getId());
        paymentRepository.save(payment);

        booking.setPaymentId(payment.getId());
        bookingRepository.save(booking);

        bookedDatesRepository.save(new BookedDates(booking.getOfferId(), booking.getId(), booking.getDateFrom(), booking.getDateUntil()));

        return booking;
    }

    /**
     * Calculates the total price for a booking based on duration and nightly rate.
     * @param startDate Booking start date.
     * @param endDate Booking end date.
     * @param pricePerNight Nightly rate of the offer.
     * @return Total booking price as BigDecimal.
     */
    private BigDecimal calculateTotalPrice(LocalDate startDate, LocalDate endDate, BigDecimal pricePerNight) {
        long days = endDate.toEpochDay() - startDate.toEpochDay() + 1;
        if (days <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking duration must be at least one day");
        }
        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }

    /**
     * Retrieves all bookings made by the authenticated user.
     * @return A list of BookingResponseDTOs with booking and offer details.
     */
    public List<BookingResponseDTO> getUserBookings() {
        logger.info("Fetching bookings for authenticated user");
        User user = getAuthenticatedUser();
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        return bookings.stream().map(booking -> {
            UUID offerId = booking.getOfferId();
            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

            String offerTitle = offer.getTitle();
            String hotelIdentifier = offer.getHotelIdentifier();

            Optional<Hotel> hotelOptional = hotelRepository.findByIdentifier(hotelIdentifier);
            String hotelName = hotelOptional.map(Hotel::getName).orElse("Unknown");
            Location location = hotelOptional.map(Hotel::getLocation).orElse(null);

            Payment payment = getPaymentOrThrow(booking.getPaymentId());

            return new BookingResponseDTO(
                    booking.getId(),
                    offerId,
                    offerTitle,
                    booking.getPrice(),
                    hotelName,
                    hotelIdentifier,
                    location,
                    booking.getDateFrom(),
                    booking.getDateUntil(),
                    booking.getStatus(),
                    payment.getStatus(),
                    booking.getExpiresAt()
            );
        }).toList();
    }

    /**
     * Retrieves a booking by ID if it belongs to the authenticated user.
     * @param id UUID of the booking.
     * @return Optional containing the booking or empty if not found or unauthorized.
     */
    public Optional<Booking> getBookingById(UUID id) {
        logger.info("Fetching booking by ID: {}", id);
        User user = getAuthenticatedUser();
        return bookingRepository.findById(id)
                .filter(booking -> booking.getUserId().equals(user.getId()));
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     * @return The authenticated User entity.
     * @throws ResponseStatusException If the user is not found or unauthorized.
     */
    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    /**
     * Cancels a pending, unpaid booking if owned by the current user.
     * Updates the payment status and removes the booking and booked dates.
     * @param bookingId UUID of the booking to cancel.
     * @return True if successfully cancelled.
     */
    public boolean cancelBooking(UUID bookingId) {
        logger.info("Cancelling booking with ID: {}", bookingId);
        Booking booking = getBookingIfOwnedByUser(bookingId);

        if (booking.getStatus() != EBookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending bookings can be cancelled");
        }

        Payment payment = getPaymentOrThrow(booking.getPaymentId());
        if (payment.getStatus() != EPaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only unpaid bookings can be cancelled");
        }

        payment.setStatus(EPaymentStatus.FAILED);
        paymentRepository.save(payment);

        bookedDatesRepository.findByOfferId(booking.getOfferId()).stream()
                .filter(bd -> bd.getDateFrom().equals(booking.getDateFrom()) &&
                        bd.getDateUntil().equals(booking.getDateUntil()))
                .forEach(bd -> bookedDatesRepository.deleteById(bd.getId()));

        bookingRepository.deleteById(bookingId);
        return true;
    }

    /**
     * Simulates a payment for a pending booking and marks it as confirmed.
     * @param bookingId UUID of the booking to simulate payment for.
     * @return The updated booking with confirmed status.
     */
    public Booking simulatePayment(UUID bookingId) {
        logger.info("Simulating payment for booking ID: {}", bookingId);
        Booking booking = checkAndFetchBooking(bookingId);

        Payment payment = getPaymentOrThrow(booking.getPaymentId());

        if (payment.getStatus() != EPaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking already paid or failed.");
        }

        payment.setStatus(EPaymentStatus.PAID);
        paymentRepository.save(payment);

        booking.setStatus(EBookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    /**
     * Retrieves the current payment status of a booking.
     * @param bookingId UUID of the booking.
     * @return The payment status of the associated payment record.
     */
    public EPaymentStatus getPaymentStatus(UUID bookingId) {
        logger.info("Retrieving payment status for booking ID: {}", bookingId);
        Booking booking = checkAndFetchBooking(bookingId);
        Payment payment = getPaymentOrThrow(booking.getPaymentId());
        return payment.getStatus();
    }

    /**
     * Retrieves a booking if it belongs to the current user and hasn't expired.
     * Cancels and removes the booking if it is expired.
     * @param bookingId UUID of the booking to validate.
     * @return The validated and fetched Booking.
     */
    private Booking checkAndFetchBooking(UUID bookingId) {
        Booking booking = getBookingIfOwnedByUser(bookingId);

        if (booking.getExpiresAt().isBefore(Instant.now())) {
            cancelBooking(bookingId);
            throw new ResponseStatusException(HttpStatus.GONE, "Booking expired and was removed.");
        }

        return booking;
    }

    /**
     * Retrieves a payment by ID or throws an error if not found.
     * @param paymentId UUID of the payment.
     * @return The Payment entity.
     */
    private Payment getPaymentOrThrow(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    /**
     * Retrieves a booking by ID only if it is owned by the authenticated user.
     * @param bookingId UUID of the booking.
     * @return The booking entity if access is allowed.
     */
    private Booking getBookingIfOwnedByUser(UUID bookingId) {
        User user = getAuthenticatedUser();
        return bookingRepository.findById(bookingId)
                .filter(b -> b.getUserId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found or access denied"));
    }

    /**
     * Deletes a booking and its related payment and booked dates if it exists.
     * @param bookingId UUID of the booking to delete.
     * @return True if deletion was successful, false otherwise.
     */
    public boolean deleteBooking(UUID bookingId) {
        logger.info("Deleting booking with ID: {}", bookingId);
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return false;
        }

        Booking booking = bookingOpt.get();

        if (booking.getPaymentId() != null) {
            paymentRepository.deleteById(booking.getPaymentId());
        }

        bookedDatesRepository.findByOfferId(booking.getOfferId()).stream()
                .filter(bd -> bd.getDateFrom().equals(booking.getDateFrom()) && bd.getDateUntil().equals(booking.getDateUntil()))
                .map(BookedDates::getId)
                .forEach(bookedDatesRepository::deleteById);
        
        bookingRepository.deleteById(bookingId);

        return true;
    }
}