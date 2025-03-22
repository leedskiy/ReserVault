package io.leedsk1y.reservault_backend.repositories;

import io.leedsk1y.reservault_backend.models.entities.Offer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfferRepository extends MongoRepository<Offer, UUID> {
    List<Offer> findByHotelIdentifier(String hotelIdentifier);
    List<Offer> findByManagerId(UUID managerId);
}