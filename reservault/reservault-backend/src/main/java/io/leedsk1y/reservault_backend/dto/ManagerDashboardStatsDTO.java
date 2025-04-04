package io.leedsk1y.reservault_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManagerDashboardStatsDTO {
    private long offers;
    private long bookings;
    private long reviews;
    private double responseRate;
}