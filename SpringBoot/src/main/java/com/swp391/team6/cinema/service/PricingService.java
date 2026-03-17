package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.Pricing;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.entity.Pricing;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.repository.PricingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRepository pricingRepository;
    private final CinemaBranchRepository branchRepository;

    public List<Pricing> getPricingByBranch(Long branchId) {

        CinemaBranch branch = branchRepository.findById(branchId).orElseThrow();

        return pricingRepository.findByBranch(branch);
    }

    public void updatePrice(Long branchId, Seat.SeatType seatType, BigDecimal price) {

        CinemaBranch branch = branchRepository.findById(branchId).orElseThrow();

        Pricing pricing = pricingRepository
                .findByBranchAndSeatType(branch, seatType)
                .orElseThrow();

        pricing.setPrice(price);

        pricingRepository.save(pricing);

    /**
     * Get price for a seat type at a branch for a given showtime start time.
     * Uses timeRange "weekend" (Sat/Sun) or "weekday" if configured in pricing table.
     */
    public BigDecimal getPrice(Long branchId, Seat.SeatType seatType, LocalDateTime showtimeStart) {
        String timeRange = isWeekend(showtimeStart) ? "weekend" : "weekday";
        return pricingRepository.findByBranchBranchIdAndSeatTypeAndTimeRange(branchId, seatType, timeRange)
                .map(Pricing::getPrice)
                .orElseGet(() -> findByBranchAndSeatTypeOnly(branchId, seatType));
    }

    private boolean isWeekend(LocalDateTime dateTime) {
        int day = dateTime.getDayOfWeek().getValue();
        return day == 6 || day == 7;
    }

    private BigDecimal findByBranchAndSeatTypeOnly(Long branchId, Seat.SeatType seatType) {
        List<Pricing> list = pricingRepository.findByBranchBranchId(branchId).stream()
                .filter(p -> p.getSeatType() == seatType)
                .toList();
        return list.isEmpty() ? BigDecimal.ZERO : list.get(0).getPrice();
    }
}
