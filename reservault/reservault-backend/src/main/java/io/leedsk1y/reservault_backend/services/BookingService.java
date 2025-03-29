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
import io.leedsk1y.reservault_backend.repositories.UserRepository;
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
    private final BookingRepository bookingRepository;
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final BookedDatesRepository bookedDatesRepository;
    private final HotelRepository hotelRepository;

    public BookingService(BookingRepository bookingRepository,
                          OfferRepository offerRepository,
                          UserRepository userRepository,
                          BookedDatesRepository bookedDatesRepository,
                          HotelRepository hotelRepository) {
        this.bookingRepository = bookingRepository;
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.bookedDatesRepository = bookedDatesRepository;
        this.hotelRepository = hotelRepository;
    }

    public Booking createBooking(Booking booking) {
        User user = getAuthenticatedUser();

        Offer offer = offerRepository.findById(booking.getOfferId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        booking.setId(UUID.randomUUID());
        booking.setUserId(user.getId());
        booking.setStatus(EBookingStatus.PENDING);
        booking.setCreatedAt(Instant.now());
        booking.setExpiresAt(booking.getCreatedAt().plusSeconds(3600)); // 1h

        if (booking.getPayment() == null) {
            booking.setPayment(new Payment());
        }

        LocalDate newStart = LocalDate.parse(booking.getDateFrom(), DateTimeFormatter.ofPattern("MM.dd.yyyy"));
        LocalDate newEnd = LocalDate.parse(booking.getDateUntil(), DateTimeFormatter.ofPattern("MM.dd.yyyy"));

        if (!newStart.isBefore(newEnd) && !newStart.equals(newEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking dates: 'dateFrom' must be before 'dateUntil'");
        }

        BigDecimal totalPrice = calculateTotalPrice(newStart, newEnd, offer.getPricePerNight());
        booking.setPrice(totalPrice);

        List<BookedDates> existingBookings = bookedDatesRepository.findByOfferId(booking.getOfferId());

        for (BookedDates existing : existingBookings) {
            LocalDate existingStart = LocalDate.parse(existing.getDateFrom(), DateTimeFormatter.ofPattern("MM.dd.yyyy"));
            LocalDate existingEnd = LocalDate.parse(existing.getDateUntil(), DateTimeFormatter.ofPattern("MM.dd.yyyy"));

            if (!(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected dates are already booked");
            }
        }

        bookingRepository.save(booking);
        bookedDatesRepository.save(new BookedDates(booking.getOfferId(), booking.getDateFrom(), booking.getDateUntil()));

        return booking;
    }

    private BigDecimal calculateTotalPrice(LocalDate startDate, LocalDate endDate, BigDecimal pricePerNight) {
        long days = endDate.toEpochDay() - startDate.toEpochDay() + 1;
        if (days <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking duration must be at least one day");
        }
        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }

    public List<BookingResponseDTO> getUserBookings() {
        User user = getAuthenticatedUser();
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        return bookings.stream().map(booking -> {
            UUID offerId = booking.getOfferId();
            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

            String offerTitle = offer.getTitle();
            BigDecimal pricePerNight = offer.getPricePerNight();
            String hotelIdentifier = offer.getHotelIdentifier();

            Optional<Hotel> hotelOptional = hotelRepository.findByIdentifier(hotelIdentifier);
            String hotelName = "Unknown";
            Location location = null;

            if (hotelOptional.isPresent()) {
                Hotel hotel = hotelOptional.get();
                hotelName = hotel.getName();
                location = hotel.getLocation();
            }

            return new BookingResponseDTO(
                    booking.getId(),
                    offerTitle,
                    booking.getPrice(),
                    hotelName,
                    location,
                    booking.getDateFrom(),
                    booking.getDateUntil(),
                    booking.getStatus(),
                    booking.getPayment().getStatus(),
                    booking.getExpiresAt()
            );
        }).toList();
    }

    public Optional<Booking> getBookingById(UUID id) {
        User user = getAuthenticatedUser();
        return bookingRepository.findById(id)
                .filter(booking -> booking.getUserId().equals(user.getId()));
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public boolean cancelBooking(UUID bookingId) {
        User user = getAuthenticatedUser();

        Booking booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getUserId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getStatus() != EBookingStatus.PENDING || booking.getPayment().getStatus() != EPaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending bookings can be cancelled");
        }

        List<BookedDates> bookedDates = bookedDatesRepository.findByOfferId(booking.getOfferId());
        bookedDates.stream()
                .filter(bd -> bd.getDateFrom().equals(booking.getDateFrom()) && bd.getDateUntil().equals(booking.getDateUntil()))
                .forEach(bd -> bookedDatesRepository.deleteById(bd.getId()));

        bookingRepository.deleteById(bookingId);
        return true;
    }

    public Booking simulatePayment(UUID bookingId) {
        Booking booking = checkAndFetchBooking(bookingId);

        if (booking.getPayment().getStatus() != EPaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking already paid or failed.");
        }

        booking.getPayment().setStatus(EPaymentStatus.PAID);
        booking.setStatus(EBookingStatus.CONFIRMED);

        return bookingRepository.save(booking);
    }

    public EPaymentStatus getPaymentStatus(UUID bookingId) {
        Booking booking = checkAndFetchBooking(bookingId);
        return booking.getPayment().getStatus();
    }

    private Booking checkAndFetchBooking(UUID bookingId) {
        User user = getAuthenticatedUser();

        Booking booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getUserId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getPayment().getStatus() == EPaymentStatus.PENDING &&
                booking.getExpiresAt().isBefore(Instant.now())) {

            cancelBooking(bookingId);
            throw new ResponseStatusException(HttpStatus.GONE, "Booking expired and was removed.");
        }

        return booking;
    }
}