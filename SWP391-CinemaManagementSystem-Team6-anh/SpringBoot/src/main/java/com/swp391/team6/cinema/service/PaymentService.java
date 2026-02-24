package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.booking.PayOsReturnRequest;
import com.swp391.team6.cinema.dto.booking.PaymentInitResponse;
import com.swp391.team6.cinema.entity.Booking;

public interface PaymentService {

    PaymentInitResponse initOnlinePayment(Booking booking);

    /**
     * Handle redirect from PayOS and update booking/payment status.
     *
     * @return bookingId related to the payment if found, otherwise null
     */
    Long handlePayOsReturn(PayOsReturnRequest request);

    void handlePayOsWebhook(String payload);
}

