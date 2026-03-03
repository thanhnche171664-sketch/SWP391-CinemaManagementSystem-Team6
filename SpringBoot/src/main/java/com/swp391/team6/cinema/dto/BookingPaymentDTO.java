package com.swp391.team6.cinema.dto;

import com.swp391.team6.cinema.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingPaymentDTO {
    private Payment.PaymentMethod method;
    private Payment.PaymentStatus status;
    private BigDecimal amount;
    private LocalDateTime paymentTime;
}
