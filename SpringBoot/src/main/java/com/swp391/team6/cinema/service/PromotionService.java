package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Promotion;
import com.swp391.team6.cinema.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    // Phân trang
    public Page<Promotion> getAllPromotions(int page,
                                            int size,
                                            String keyword,
                                            String status,
                                            String discountType,
                                            String timeFilter) {
        List<Promotion> allPromotions = promotionRepository.findAll(Sort.by("promotionId").descending());
        return buildPromotionPage(allPromotions, page, size, keyword, status, discountType, timeFilter);
    }

    public Page<Promotion> getPromotionsForBranch(Long branchId,
                                                  int page,
                                                  int size,
                                                  String keyword,
                                                  String status,
                                                  String discountType,
                                                  String timeFilter) {
        List<Promotion> promotions = promotionRepository.findAllByBranch_BranchId(branchId, Sort.by("promotionId").descending());
        return buildPromotionPage(promotions, page, size, keyword, status, discountType, timeFilter);
    }

    public List<Promotion> getPromotionsByBranch(Long branchId) {
        return promotionRepository.findAllByBranch_BranchId(branchId, Sort.by("promotionId").descending());
    }


    // Thống kê
    public long countAll() {
        return promotionRepository.count();
    }

    public long countActive() {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.countActive(now);
    }

    public long countInactive() {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.countInactive(now);
    }

    // Tìm kiếm theo ID
    public Promotion findById(Integer id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mã khuyến mãi không tồn tại: " + id));
    }

    @Transactional
    public void create(Promotion promotion) {

        // ===================== 1. Validate mã khuyến mãi =====================
        if (promotion.getPromoCode() == null || promotion.getPromoCode().trim().isEmpty()) {
            throw new RuntimeException("Mã khuyến mãi không được để trống");
        }

        if (promotionRepository.existsByPromoCode(promotion.getPromoCode().trim())) {
            throw new RuntimeException("Mã khuyến mãi '" + promotion.getPromoCode() + "' đã tồn tại!");
        }

        // ===================== 2. Validate tên khuyến mãi =====================
        if (promotion.getDescription() == null || promotion.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Tên khuyến mãi không được để trống");
        }

        // ===================== 3. Validate loại giảm giá =====================
        if (promotion.getDiscountType() == null) {
            throw new RuntimeException("Vui lòng chọn loại khuyến mãi");
        }

        // ===================== 4. Validate giá trị giảm =====================
        if (promotion.getDiscountValue() == null) {
            throw new RuntimeException("Giá trị giảm không được để trống");
        }

        if (promotion.getDiscountValue().doubleValue() <= 0) {
            throw new RuntimeException("Giá trị giảm phải lớn hơn 0");
        }

        // Nếu giảm theo %
        if (promotion.getDiscountType() == Promotion.DiscountType.percent) {
            if (promotion.getDiscountValue().doubleValue() > 100) {
                throw new RuntimeException("Giảm theo % không được lớn hơn 100%");
            }
        }

        // ===================== 5. Validate đơn hàng tối thiểu =====================
        if (promotion.getMinBookingAmount() != null &&
                promotion.getMinBookingAmount().doubleValue() < 0) {
            throw new RuntimeException("Đơn hàng tối thiểu không được là số âm");
        }

        // ===================== 6. Validate giới hạn sử dụng =====================
        if (promotion.getUsageLimit() == null) {
            throw new RuntimeException("Vui lòng nhập giới hạn số lần sử dụng");
        }

        if (promotion.getUsageLimit() < 0) {
            throw new RuntimeException("Giới hạn sử dụng không được là số âm");
        }

        // ===================== 7. Validate ngày bắt đầu - kết thúc =====================
        if (promotion.getStartDate() == null) {
            throw new RuntimeException("Ngày bắt đầu không được để trống");
        }

        if (promotion.getEndDate() == null) {
            throw new RuntimeException("Ngày kết thúc không được để trống");
        }

        if (promotion.getStartDate().isAfter(promotion.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        // Không cho tạo khuyến mãi đã hết hạn
        if (promotion.getEndDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Ngày kết thúc phải lớn hơn thời điểm hiện tại");
        }

        // ===================== 8. Default value =====================
        if (promotion.getStatus() == null) {
            promotion.setStatus(Promotion.Status.active);
        }

        if (promotion.getUsedCount() == null) {
            promotion.setUsedCount(0);
        }

        if (promotion.getPromoCode() != null) {
            promotion.setPromoCode(promotion.getPromoCode().trim().toUpperCase());
        }

        // ===================== 9. Save =====================
        promotionRepository.save(promotion);
    }

    @Transactional
    public void update(Promotion promotion) {
        // ===================== 1. Lấy promotion hiện tại =====================
        Promotion existing = findById(promotion.getPromotionId());

        if (existing == null) {
            throw new RuntimeException("Không tìm thấy khuyến mãi");
        }

        // ===================== 2. Validate mã code =====================
        if (promotion.getPromoCode() == null || promotion.getPromoCode().trim().isEmpty()) {
            throw new RuntimeException("Mã khuyến mãi không được để trống");
        }

        // Nếu code bị đổi thì mới check trùng
        if (!existing.getPromoCode().equalsIgnoreCase(promotion.getPromoCode().trim())) {
            if (promotionRepository.existsByPromoCode(promotion.getPromoCode().trim())) {
                throw new RuntimeException("Mã khuyến mãi đã tồn tại");
            }
        }

        // ===================== 3. Validate tên =====================
        if (promotion.getDescription() == null || promotion.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Tên khuyến mãi không được để trống");
        }

        // ===================== 4. Validate discount =====================
        if (promotion.getDiscountValue() == null) {
            throw new RuntimeException("Giá trị giảm không được để trống");
        }

        if (promotion.getDiscountValue().doubleValue() <= 0) {
            throw new RuntimeException("Giá trị giảm phải lớn hơn 0");
        }

        if (promotion.getDiscountType() == Promotion.DiscountType.percent &&
                promotion.getDiscountValue().doubleValue() > 100) {
            throw new RuntimeException("Giảm theo % không được lớn hơn 100%");
        }

        // ===================== 5. Validate min booking =====================
        if (promotion.getMinBookingAmount() != null &&
                promotion.getMinBookingAmount().doubleValue() < 0) {
            throw new RuntimeException("Đơn hàng tối thiểu không được là số âm");
        }

        // ===================== 6. Validate usage limit =====================
        if (promotion.getUsageLimit() == null) {
            throw new RuntimeException("Giới hạn sử dụng không được để trống");
        }

        if (promotion.getUsageLimit() < 0) {
            throw new RuntimeException("Giới hạn sử dụng không được là số âm");
        }

        // Không cho sửa usageLimit nhỏ hơn số lần đã dùng
        if (promotion.getUsageLimit() < existing.getUsedCount()) {
            throw new RuntimeException("Giới hạn sử dụng không được nhỏ hơn số lần đã sử dụng");
        }

        // ===================== 7. Validate ngày tháng =====================
        if (promotion.getStartDate() == null) {
            throw new RuntimeException("Ngày bắt đầu không được để trống");
        }

        if (promotion.getEndDate() == null) {
            throw new RuntimeException("Ngày kết thúc không được để trống");
        }

        if (promotion.getStartDate().isAfter(promotion.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        // ===================== 8. Update dữ liệu =====================
        existing.setPromoCode(promotion.getPromoCode().trim().toUpperCase());
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
        public void softDelete (Integer id){
            promotionRepository.findById(id).ifPresent(promo -> {
                promo.setStatus(Promotion.Status.inactive);
                promotionRepository.save(promo);
            });
        }

    private Page<Promotion> buildPromotionPage(List<Promotion> promotions,
                                               int page,
                                               int size,
                                               String keyword,
                                               String status,
                                               String discountType,
                                               String timeFilter) {
        applyExpiryStatus(promotions);
        LocalDateTime now = LocalDateTime.now();

        List<Promotion> filteredList = promotions.stream()
                .filter(p -> {
                    if (status != null && !status.isEmpty()) {
                        return p.getStatus().name().equalsIgnoreCase(status);
                    }
                    return true;
                })
                .filter(p -> {
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        String k = keyword.toLowerCase();
                        boolean matchCode = p.getPromoCode().toLowerCase().contains(k);
                        boolean matchDesc = (p.getDescription() != null && p.getDescription().toLowerCase().contains(k));
                        return matchCode || matchDesc;
                    }
                    return true;
                })
                .filter(p -> {
                    if (discountType != null && !discountType.isBlank()) {
                        return p.getDiscountType() != null
                                && p.getDiscountType().name().equalsIgnoreCase(discountType);
                    }
                    return true;
                })
                .filter(p -> {
                    if (timeFilter == null || timeFilter.isBlank()) {
                        return true;
                    }

                    return switch (timeFilter.toLowerCase()) {
                        case "upcoming" -> p.getStartDate() != null && p.getStartDate().isAfter(now);
                        case "ongoing" ->
                                (p.getStartDate() == null || !p.getStartDate().isAfter(now))
                                        && (p.getEndDate() == null || !p.getEndDate().isBefore(now));
                        case "expired" -> p.getEndDate() != null && p.getEndDate().isBefore(now);
                        default -> true;
                    };
                })
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredList.size());

        List<Promotion> pagedList = new ArrayList<>();
        if (start <= filteredList.size()) {
            pagedList = filteredList.subList(start, end);
        }

        return new PageImpl<>(pagedList, pageable, filteredList.size());
    }

    private void applyExpiryStatus(List<Promotion> promotions) {
        LocalDateTime now = LocalDateTime.now();
        promotions.forEach(p -> {
            if (p.getEndDate() != null && p.getEndDate().isBefore(now)) {
                p.setStatus(Promotion.Status.inactive);
            }
        });
    }

    public Map<String, Object> validatePromoCode(String code, BigDecimal orderAmount) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", false);
        result.put("message", "Mã voucher không hợp lệ");
        result.put("discountAmount", 0);
        result.put("promotionId", null);

        if (code == null || code.trim().isEmpty()) {
            return result;
        }

        try {
            Optional<Promotion> promoOpt = promotionRepository.findByPromoCode(code.trim().toUpperCase());
            if (promoOpt.isEmpty()) {
                result.put("message", "Mã voucher không tồn tại");
                return result;
            }

            Promotion promo = promoOpt.get();

            // Check status
            if (promo.getStatus() != Promotion.Status.active) {
                result.put("message", "Mã voucher đã ngừng hoạt động");
                return result;
            }

            // Check date
            LocalDateTime now = LocalDateTime.now();
            if (promo.getStartDate() != null && now.isBefore(promo.getStartDate())) {
                result.put("message", "Mã voucher chưa có hiệu lực");
                return result;
            }
            if (promo.getEndDate() != null && now.isAfter(promo.getEndDate())) {
                result.put("message", "Mã voucher đã hết hạn");
                return result;
            }

            // Check usage limit
            if (promo.getUsageLimit() != null && promo.getUsedCount() != null
                    && promo.getUsedCount() >= promo.getUsageLimit()) {
                result.put("message", "Mã voucher đã hết lượt sử dụng");
                return result;
            }

            // Check minimum booking amount
            if (promo.getMinBookingAmount() != null && orderAmount != null
                    && orderAmount.compareTo(promo.getMinBookingAmount()) < 0) {
                result.put("message", "Đơn hàng tối thiểu " + promo.getMinBookingAmount().toBigInteger() + " VND");
                return result;
            }

            // Calculate discount
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (promo.getDiscountType() == Promotion.DiscountType.percent) {
                discountAmount = orderAmount.multiply(promo.getDiscountValue())
                        .divide(BigDecimal.valueOf(100));
            } else if (promo.getDiscountType() == Promotion.DiscountType.amount) {
                discountAmount = promo.getDiscountValue();
            }

            // Ensure discount doesn't exceed order amount
            if (discountAmount.compareTo(orderAmount) > 0) {
                discountAmount = orderAmount;
            }

            result.put("valid", true);
            result.put("message", "Áp dụng thành công!");
            result.put("discountAmount", discountAmount.intValue());
            result.put("promotionId", promo.getPromotionId());

        } catch (Exception e) {
            result.put("message", "Lỗi xác thực voucher: " + e.getMessage());
        }

        return result;
    }

    public List<Promotion> getValidPromotions() {
        return promotionRepository.findAllValidPromotions();
    }
}
