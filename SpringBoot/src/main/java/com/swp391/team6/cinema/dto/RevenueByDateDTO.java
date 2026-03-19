package com.swp391.team6.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueByDateDTO {
    private LocalDate date;
    private long bookingCount;
    private BigDecimal totalRevenue;
}
