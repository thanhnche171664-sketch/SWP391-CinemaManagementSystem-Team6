package com.swp391.team6.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueSummaryDTO {
    private BigDecimal totalRevenue;
    private long totalBookings;
    private BigDecimal avgRevenue;
    private long totalMovies;
    private long totalBranches;
}
