package com.swp391.team6.cinema.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayOsReturnRequest {

    /**
     * PayOS order code used to correlate with internal Payment/Booking.
     */
    private String orderCode;

    /**
     * High-level status returned by PayOS on redirect (e.g. SUCCESS, CANCEL, FAILED).
     * Webhook remains the source of truth for final payment status.
     */
    private String status;

    /**
     * Optional message/description from PayOS.
     */
    private String message;
}

