package io.leedsk1y.reservault_backend.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Facilities {
    private boolean wifi;
    private boolean parking;
    private boolean pool;
    private boolean airConditioning;
    private boolean breakfast;
}