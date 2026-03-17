package com.swp391.team6.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueByMovieDTO {
    private Long movieId;
    private String movieTitle;
    private long bookingCount;
    private BigDecimal totalRevenue;
}
