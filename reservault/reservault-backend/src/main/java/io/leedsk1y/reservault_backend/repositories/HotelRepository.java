package io.leedsk1y.reservault_backend.repositories;

import io.leedsk1y.reservault_backend.models.entities.Hotel;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.UUID;

public interface HotelRepository extends MongoRepository<Hotel, UUID> {
}
