package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.Pricing;
import com.swp391.team6.cinema.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRepository extends JpaRepository<Pricing, Long> {

    List<Pricing> findByBranch(CinemaBranch branchId);
    List<Pricing> findByBranchBranchId(Long branchId);
    
    List<Pricing> findBySeatType(Seat.SeatType seatType);

    Optional<Pricing> findByBranchAndSeatType(
            CinemaBranch branch,
            Seat.SeatType seatType
    );
    java.util.Optional<Pricing> findByBranchBranchIdAndSeatTypeAndTimeRange(Long branchId, Seat.SeatType seatType, String timeRange);
}
