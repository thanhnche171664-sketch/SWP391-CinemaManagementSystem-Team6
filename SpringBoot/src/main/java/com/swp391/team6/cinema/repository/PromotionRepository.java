package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p FROM Promotion p WHERE p.status = 'active' " +
            "AND p.startDate <= CURRENT_TIMESTAMP AND p.endDate >= CURRENT_TIMESTAMP " +
            "AND (p.branch IS NULL OR p.branch.branchId = :branchId) " +
            "AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)")
    List<Promotion> findActivePromotionsForBranch(@Param("branchId") Long branchId);

    Promotion findByPromoCode(String promoCode);
}
