package io.leedsk1y.reservault_backend.models.entities;

import io.leedsk1y.reservault_backend.models.enums.EPaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Payment {
    public Payment() {
        this.status = EPaymentStatus.PENDING;
        this.createdAt = Instant.now();
    }

    private EPaymentStatus status;
    private Instant createdAt;
}
