package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.models.entities.Booking;
import io.leedsk1y.reservault_backend.models.enums.EBookingStatus;
import io.leedsk1y.reservault_backend.models.enums.EPaymentStatus;
import io.leedsk1y.reservault_backend.repositories.BookedDatesRepository;
import io.leedsk1y.reservault_backend.repositories.BookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class BookingCleanupService {

    private final BookingRepository bookingRepository;
    private final BookedDatesRepository bookedDatesRepository;

    public BookingCleanupService(BookingRepository bookingRepository, BookedDatesRepository bookedDatesRepository) {
        this.bookingRepository = bookingRepository;
        this.bookedDatesRepository = bookedDatesRepository;
    }

    @Scheduled(fixedRate = 10 * 60 * 1000) // 10min
    public void cleanExpiredBookings() {
        List<Booking> expiredBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == EBookingStatus.PENDING)
                .filter(b -> b.getPayment().getStatus() == EPaymentStatus.PENDING)
                .filter(b -> b.getExpiresAt().isBefore(Instant.now()))
                .toList();

        for (Booking booking : expiredBookings) {
            bookedDatesRepository.findByOfferId(booking.getOfferId()).stream()
                    .filter(bd -> bd.getDateFrom().equals(booking.getDateFrom()) &&
                            bd.getDateUntil().equals(booking.getDateUntil()))
                    .forEach(bd -> bookedDatesRepository.deleteById(bd.getId()));

            bookingRepository.deleteById(booking.getId());
        }
    }
}