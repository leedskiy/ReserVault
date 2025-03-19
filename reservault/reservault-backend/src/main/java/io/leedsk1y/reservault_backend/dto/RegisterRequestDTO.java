package io.leedsk1y.reservault_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RegisterRequestDTO {
    private String name;
    private String email;
    private String password;
    private boolean isManager;
    private List<String> hotelIdentifiers;

    public boolean getIsManager() {
        return isManager;
    }
}