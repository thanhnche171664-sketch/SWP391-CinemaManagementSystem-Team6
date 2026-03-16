package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.Pricing;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.repository.PricingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    }
}
