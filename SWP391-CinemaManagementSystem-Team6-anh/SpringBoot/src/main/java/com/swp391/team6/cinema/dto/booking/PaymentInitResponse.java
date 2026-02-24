package com.swp391.team6.cinema.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitResponse {

    private Long bookingId;
    private String orderCode;
    private String paymentUrl;
}

