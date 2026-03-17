package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.RevenueByBranchDTO;
import com.swp391.team6.cinema.dto.RevenueByDateDTO;
import com.swp391.team6.cinema.dto.RevenueByMovieDTO;
import com.swp391.team6.cinema.dto.RevenueReportDTO;
import com.swp391.team6.cinema.dto.RevenueSummaryDTO;
import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.entity.Showtime;
import com.swp391.team6.cinema.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public RevenueReportDTO buildRevenueReport(Long branchId) {
        List<Booking> bookings = branchId == null
                ? bookingRepository.findAllWithDetails()
                : bookingRepository.findByBranchIdWithDetails(branchId);

        List<Booking> paidBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.paid)
                .collect(Collectors.toList());

        Map<Long, RevenueByMovieDTO> movieMap = new HashMap<>();
        Map<LocalDate, RevenueByDateDTO> dateMap = new HashMap<>();
        Map<Long, RevenueByBranchDTO> branchMap = new HashMap<>();

        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Booking booking : paidBookings) {
            BigDecimal amount = safeAmount(booking.getTotalAmount());
            totalRevenue = totalRevenue.add(amount);

            Showtime showtime = booking.getShowtime();
            if (showtime != null) {
                Movie movie = showtime.getMovie();
                if (movie != null) {
                    RevenueByMovieDTO row = movieMap.computeIfAbsent(
                            movie.getMovieId(),
                            id -> new RevenueByMovieDTO(id, movie.getTitle(), 0, BigDecimal.ZERO)
                    );
                    row.setBookingCount(row.getBookingCount() + 1);
                    row.setTotalRevenue(row.getTotalRevenue().add(amount));
                }

                if (showtime.getRoom() != null) {
                    CinemaBranch branch = showtime.getRoom().getBranch();
                    if (branch != null) {
                        RevenueByBranchDTO row = branchMap.computeIfAbsent(
                                branch.getBranchId(),
                                id -> new RevenueByBranchDTO(id, branch.getBranchName(), 0, BigDecimal.ZERO)
                        );
                        row.setBookingCount(row.getBookingCount() + 1);
                        row.setTotalRevenue(row.getTotalRevenue().add(amount));
                    }
                }
            }

            if (booking.getBookingTime() != null) {
                LocalDate date = booking.getBookingTime().toLocalDate();
                RevenueByDateDTO row = dateMap.computeIfAbsent(
                        date,
                        d -> new RevenueByDateDTO(d, 0, BigDecimal.ZERO)
                );
                row.setBookingCount(row.getBookingCount() + 1);
                row.setTotalRevenue(row.getTotalRevenue().add(amount));
            }
        }

        List<RevenueByMovieDTO> revenueByMovie = new ArrayList<>(movieMap.values());
        revenueByMovie.sort(Comparator.comparing(RevenueByMovieDTO::getTotalRevenue).reversed());

        List<RevenueByDateDTO> revenueByDate = new ArrayList<>(dateMap.values());
        revenueByDate.sort(Comparator.comparing(RevenueByDateDTO::getDate).reversed());

        List<RevenueByBranchDTO> revenueByBranch = new ArrayList<>(branchMap.values());
        revenueByBranch.sort(Comparator.comparing(RevenueByBranchDTO::getTotalRevenue).reversed());

        long totalBookings = paidBookings.size();
        BigDecimal avgRevenue = totalBookings == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(totalBookings), 2, RoundingMode.HALF_UP);

        RevenueSummaryDTO summary = new RevenueSummaryDTO(
                totalRevenue,
                totalBookings,
                avgRevenue,
                movieMap.size(),
                branchMap.size()
        );

        return new RevenueReportDTO(summary, revenueByMovie, revenueByDate, revenueByBranch);
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
