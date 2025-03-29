package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.models.entities.BookedDates;
import io.leedsk1y.reservault_backend.models.entities.Booking;
import io.leedsk1y.reservault_backend.models.entities.Payment;
import io.leedsk1y.reservault_backend.models.enums.EBookingStatus;
import io.leedsk1y.reservault_backend.models.enums.EPaymentStatus;
import io.leedsk1y.reservault_backend.repositories.BookedDatesRepository;
import io.leedsk1y.reservault_backend.repositories.BookingRepository;
import io.leedsk1y.reservault_backend.repositories.PaymentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BookingCleanupService {

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

    @Scheduled(fixedRate = 10 * 60 * 1000) // 10min
    public void cleanExpiredBookings() {
        List<Booking> expiredBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == EBookingStatus.PENDING)
                .filter(b -> b.getExpiresAt().isBefore(Instant.now()))
                .toList();

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