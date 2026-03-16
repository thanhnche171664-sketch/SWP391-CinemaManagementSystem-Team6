package com.swp391.team6.cinema.entity;

import ch.qos.logback.core.status.Status;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promotionId;

    @Column(nullable = false, unique = true)
    private String promoCode;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private BigDecimal discountValue;

    @Column(columnDefinition = "DECIMAL(10,2) DEFAULT 0")
    private BigDecimal minBookingAmount = BigDecimal.ZERO;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    // Liên kết với nhánh (Null nếu áp dụng toàn quốc)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private CinemaBranch branch;

    private Integer usageLimit;

    private Integer usedCount = 0;

    @Enumerated(EnumType.STRING)
    private Status status = Status.active;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        active, inactive
    }

    public enum DiscountType {
        percent, amount
    }
}