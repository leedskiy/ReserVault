package io.leedsk1y.reservault_backend.repositories;

import io.leedsk1y.reservault_backend.models.entities.BookedDates;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface BookedDatesRepository extends MongoRepository<BookedDates, UUID> {
    List<BookedDates> findByOfferId(UUID offerId);
}