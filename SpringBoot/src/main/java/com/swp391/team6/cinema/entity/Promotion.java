package com.swp391.team6.cinema.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
@Getter
@Setter
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Integer promotionId;

    @Column(name = "promo_code", unique = true, nullable = false)
    private String promoCode;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType; // percent, amount

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "min_booking_amount")
    private BigDecimal minBookingAmount;

    /**
     * Sửa lỗi: Thêm @DateTimeFormat để nhận dữ liệu từ <input type="date">
     * Spring sẽ tự động thêm giờ 00:00:00 vào ngày bạn chọn.
     */
    @Column(name = "start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Enumerated(EnumType.STRING)
    private Status status = Status.active;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private CinemaBranch branch;

    public enum DiscountType { percent, amount }
    public enum Status { active, inactive }
}