package io.leedsk1y.reservault_backend.models.entities;

import io.leedsk1y.reservault_backend.models.enums.ERole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "roles")
public class Role {
    @Id
    private UUID id;

    private ERole name;

    public Role(ERole name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }
}