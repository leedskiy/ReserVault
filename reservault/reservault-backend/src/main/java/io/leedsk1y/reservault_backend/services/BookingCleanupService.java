package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.models.entities.BookedDates;
import io.leedsk1y.reservault_backend.models.entities.Booking;
import io.leedsk1y.reservault_backend.models.enums.EBookingStatus;
import io.leedsk1y.reservault_backend.models.enums.EPaymentStatus;
import io.leedsk1y.reservault_backend.repositories.BookedDatesRepository;
import io.leedsk1y.reservault_backend.repositories.BookingRepository;
import io.leedsk1y.reservault_backend.repositories.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BookingCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(BookingCleanupService.class);
    private final BookingRepository bookingRepository;
    private final BookedDatesRepository bookedDatesRepository;
    private final PaymentRepository paymentRepository;

    public BookingCleanupService(
            BookingRepository bookingRepository,
            BookedDatesRepository bookedDatesRepository,
            PaymentRepository paymentRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.bookedDatesRepository = bookedDatesRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Scheduled task that runs every 10 minutes to clean up expired bookings.
     * Marks associated pending payments as failed and deletes booked dates and booking records.
     */
    @Scheduled(fixedRate = 10 * 60 * 1000) // 10min
    public void cleanExpiredBookings() {
        logger.info("Running scheduled task: cleanExpiredBookings");

        List<Booking> expiredBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == EBookingStatus.PENDING)
                .filter(b -> b.getExpiresAt().isBefore(Instant.now()))
                .toList();

        logger.info("Found {} expired bookings to process", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            UUID paymentId = booking.getPaymentId();

            paymentRepository.findById(paymentId).ifPresent(payment -> {
                if (payment.getStatus() == EPaymentStatus.PENDING) {
                    payment.setStatus(EPaymentStatus.FAILED);
                    paymentRepository.save(payment);
                }
            });

            bookedDatesRepository.findByOfferId(booking.getOfferId()).stream()
                    .filter(bd -> bd.getDateFrom().equals(booking.getDateFrom()) &&
                            bd.getDateUntil().equals(booking.getDateUntil()))
                    .map(BookedDates::getId)
                    .forEach(bookedDatesRepository::deleteById);

            bookingRepository.deleteById(booking.getId());
        }
    }
}