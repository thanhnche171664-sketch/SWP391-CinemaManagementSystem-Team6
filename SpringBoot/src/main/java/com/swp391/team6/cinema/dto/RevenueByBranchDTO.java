package com.swp391.team6.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueByBranchDTO {
    private Long branchId;
    private String branchName;
    private long bookingCount;
    private BigDecimal totalRevenue;
}
