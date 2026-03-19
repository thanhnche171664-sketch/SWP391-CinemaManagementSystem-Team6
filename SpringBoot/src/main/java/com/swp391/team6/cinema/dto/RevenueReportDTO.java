package com.swp391.team6.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDTO {
    private RevenueSummaryDTO summary;
    private List<RevenueByMovieDTO> revenueByMovie;
    private List<RevenueByDateDTO> revenueByDate;
    private List<RevenueByBranchDTO> revenueByBranch;
}
