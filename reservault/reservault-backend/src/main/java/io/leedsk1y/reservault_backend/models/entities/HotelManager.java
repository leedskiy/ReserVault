package io.leedsk1y.reservault_backend.models.entities;

import io.leedsk1y.reservault_backend.models.enums.EHotelManagerStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "hotel_managers")
public class HotelManager {
    public HotelManager(String hotelIdentifier, UUID managerId) {
        this.id = UUID.randomUUID();
        this.hotelIdentifier = hotelIdentifier;
        this.managerId = managerId;
        this.status = EHotelManagerStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public HotelManager(String hotelIdentifier, UUID managerId, EHotelManagerStatus status) {
        this.id = UUID.randomUUID();
        this.hotelIdentifier = hotelIdentifier;
        this.managerId = managerId;
        this.status = status;
        this.createdAt = Instant.now();
    }

    @Id
    private UUID id;

    private String hotelIdentifier;

    private UUID managerId;

    private EHotelManagerStatus status;

    @CreatedDate
    private Instant createdAt;
}