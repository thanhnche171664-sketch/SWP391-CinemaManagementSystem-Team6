package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Promotion;
import com.swp391.team6.cinema.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    // Phân trang
    public Page<Promotion> getAllPromotions(int page, int size, String keyword, String status) {
        List<Promotion> allPromotions = promotionRepository.findAll(Sort.by("promotionId").descending());

        // 2. Lọc bằng Stream API
        List<Promotion> filteredList = allPromotions.stream()
                .filter(p -> {
                    // Lọc theo trạng thái
                    if (status != null && !status.isEmpty()) {
                        return p.getStatus().name().equalsIgnoreCase(status);
                    }
                    return true;
                })
                .filter(p -> {
                    // Lọc theo từ khóa (Code hoặc Description)
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        String k = keyword.toLowerCase();
                        boolean matchCode = p.getPromoCode().toLowerCase().contains(k);
                        boolean matchDesc = (p.getDescription() != null && p.getDescription().toLowerCase().contains(k));
                        return matchCode || matchDesc;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 3. Phân trang thủ công cho List kết quả
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());

        List<Promotion> pagedList = new ArrayList<>();
        if (start <= filteredList.size()) {
            pagedList = filteredList.subList(start, end);
        }

        return new PageImpl<>(pagedList, pageable, filteredList.size());
    }


    // Thống kê
    public long countAll() { return promotionRepository.count(); }
    public long countActive() { return promotionRepository.countByStatus(Promotion.Status.active); }
    public long countInactive() { return promotionRepository.countByStatus(Promotion.Status.inactive); }

    // Tìm kiếm theo ID
    public Promotion findById(Integer id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mã khuyến mãi không tồn tại: " + id));
    }

    @Transactional
    public void create(Promotion promotion) {
        // Business Logic: Mặc định giá trị ban đầu
        if (promotion.getStatus() == null) promotion.setStatus(Promotion.Status.active);
        if (promotion.getUsedCount() == null) promotion.setUsedCount(0);
        promotionRepository.save(promotion);
    }

    @Transactional
    public void update(Promotion promotion) {
        Promotion existing = findById(promotion.getPromotionId());

        // Cập nhật các trường dữ liệu
        existing.setPromoCode(promotion.getPromoCode());
        existing.setDescription(promotion.getDescription());
        existing.setDiscountType(promotion.getDiscountType());
        existing.setDiscountValue(promotion.getDiscountValue());
        existing.setStartDate(promotion.getStartDate());
        existing.setEndDate(promotion.getEndDate());
        existing.setUsageLimit(promotion.getUsageLimit());
        existing.setMinBookingAmount(promotion.getMinBookingAmount());

        promotionRepository.save(existing);
    }

    @Transactional
    public void softDelete(Integer id) {
        promotionRepository.findById(id).ifPresent(promo -> {
            promo.setStatus(Promotion.Status.inactive);
            promotionRepository.save(promo);
        });
    }
}