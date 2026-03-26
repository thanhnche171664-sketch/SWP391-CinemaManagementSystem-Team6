package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    // Tìm kiếm theo code (Màn hình List)
    Optional<Promotion> findByPromoCode(String promoCode);

    // Lọc theo chi nhánh (Dành cho Branch Manager)
    List<Promotion> findAllByBranch_BranchIdOrBranchIsNull(Long branchId);

    List<Promotion> findAllByBranch_BranchId(Long branchId);

    boolean existsByPromoCode(String promoCode); //check promotion code đã tồn tại hay chưa

    List<Promotion> findAllByBranch_BranchId(Long branchId, Sort sort);

    // Thống kê số lượng theo trạng thái (Cho 4 ô dashboard)
    long countByStatus(Promotion.Status status);

    @Query("""
    SELECT COUNT(p)
    FROM Promotion p
    WHERE p.status = 'active'
      AND (p.endDate IS NULL OR p.endDate >= :now)
""")
    long countActive(@Param("now") LocalDateTime now);

    @Query("""
    SELECT COUNT(p)
    FROM Promotion p
    WHERE p.status = 'inactive'
       OR (p.endDate IS NOT NULL AND p.endDate < :now)
""")
    long countInactive(@Param("now") LocalDateTime now);

    boolean existsByPromoCodeAndPromotionIdNot(String promoCode, Integer promotionId);

    Page<Promotion> findByPromoCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String promoCode, String description, Pageable pageable);
    @Query("SELECT p FROM Promotion p WHERE p.status = 'active' " +
            "AND p.startDate <= CURRENT_TIMESTAMP AND p.endDate >= CURRENT_TIMESTAMP " +
            "AND (p.branch IS NULL OR p.branch.branchId = :branchId) " +
            "AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)")
    List<Promotion> findActivePromotionsForBranch(@Param("branchId") Long branchId);

    @Query("""
    SELECT p
    FROM Promotion p
    WHERE p.status = 'active'
      AND (p.startDate IS NULL OR p.startDate <= CURRENT_TIMESTAMP)
      AND (p.endDate IS NULL OR p.endDate >= CURRENT_TIMESTAMP)
      AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)
""")
    List<Promotion> findAllValidPromotions();
}
