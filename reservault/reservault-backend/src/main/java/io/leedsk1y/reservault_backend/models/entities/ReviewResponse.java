package io.leedsk1y.reservault_backend.models.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ReviewResponse {
    public ReviewResponse(UUID managerId, String comment) {
        this.managerId = managerId;
        this.comment = comment;
        this.createdAt = Instant.now();
    }

    private UUID managerId;

    private String comment;

    private Instant createdAt;
}