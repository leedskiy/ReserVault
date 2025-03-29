package io.leedsk1y.reservault_backend.models.entities;

import io.leedsk1y.reservault_backend.models.enums.EPaymentStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "payments")
public class Payment {
    public Payment() {
        this.createdAt = Instant.now();
        this.status = EPaymentStatus.PENDING;
    }

    @Id
    private UUID id;

    private UUID bookingId;

    private EPaymentStatus status;

    private Instant createdAt;
}
