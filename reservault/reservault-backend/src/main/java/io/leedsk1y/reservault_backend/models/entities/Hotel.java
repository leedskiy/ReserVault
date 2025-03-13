package io.leedsk1y.reservault_backend.models.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "hotels")
public class Hotel {
    public Hotel() {
        this.imagesUrls = new ArrayList<>();
    }

    public Hotel(String name, String description, List<String> imagesUrls, int stars, Location location) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.imagesUrls = imagesUrls;
        this.stars = stars;
        this.location = location;
        this.createdAt = Instant.now();
    }

    @Id
    private UUID id;

    private String name;

    private String description;

    private List<String> imagesUrls;

    private int stars;

    private Location location;

    @CreatedDate
    private Instant createdAt;
}
