package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    // Tìm kiếm theo code (Màn hình List)
    Optional<Promotion> findByPromoCode(String promoCode);

    // Lọc theo chi nhánh (Dành cho Branch Manager)
    List<Promotion> findAllByBranch_BranchIdOrBranchIsNull(Integer branchId);

    // Thống kê số lượng theo trạng thái (Cho 4 ô dashboard)
    long countByStatus(Promotion.Status status);


    boolean existsByPromoCodeAndPromotionIdNot(String promoCode, Integer promotionId);

    Page<Promotion> findByPromoCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String promoCode, String description, Pageable pageable);
}
