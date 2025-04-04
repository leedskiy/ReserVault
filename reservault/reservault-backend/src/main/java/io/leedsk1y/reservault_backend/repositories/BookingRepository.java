package io.leedsk1y.reservault_backend.repositories;

import io.leedsk1y.reservault_backend.models.entities.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends MongoRepository<Booking, UUID> {
    List<Booking> findByUserId(UUID userId);
    List<Booking> findByOfferId(UUID offerId);
    long countByOfferId(UUID offerId);
}