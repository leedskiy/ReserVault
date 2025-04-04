package io.leedsk1y.reservault_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardStatsDTO {
    private long totalUsers;
    private long verifiedManagers;
    private long totalHotels;
    private long totalOffers;
    private long totalManagers;
    private long totalBookings;
}
