package com.swp391.team6.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "method")
    private PaymentMethod method;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_status",
            length = 20,
            columnDefinition = "ENUM('pending','success','failed','cancelled')"
    )
    private PaymentStatus paymentStatus;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime = LocalDateTime.now();

    @Column(name = "order_code", unique = true)
    private Long orderCode;

    @Column(name = "payment_link_id", length = 100)
    private String paymentLinkId;

    public enum PaymentMethod {
        cash, card, online
    }

    public enum PaymentStatus {
        pending, success, failed, cancelled
    }
}
