package io.leedsk1y.reservault_backend.repositories;

import io.leedsk1y.reservault_backend.models.entities.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface PaymentRepository extends MongoRepository<Payment, UUID> {
}
