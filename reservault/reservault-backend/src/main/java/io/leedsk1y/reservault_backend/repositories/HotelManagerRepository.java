package io.leedsk1y.reservault_backend.repositories;

import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HotelManagerRepository extends MongoRepository<HotelManager, UUID> {
    List<HotelManager> findByManagerId(UUID managerId);
    void deleteByManagerId(UUID managerId);
    void deleteByManagerIdAndHotelIdentifierIn(UUID managerId, List<String> hotelIdentifiers);
    Optional<HotelManager> findByManagerIdAndHotelIdentifier(UUID managerId, String hotelIdentifier);
    void deleteByHotelIdentifier(String hotelIdentifier);
}