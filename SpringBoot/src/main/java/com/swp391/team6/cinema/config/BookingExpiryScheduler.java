package com.swp391.team6.cinema.config;

import com.swp391.team6.cinema.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpiryScheduler {

    private final BookingService bookingService;

    /** Run every 2 minutes to cancel pending bookings that exceeded payment time limit. */
    @Scheduled(fixedRate = 120000)
    public void cancelExpiredPendingBookings() {
        try {
            bookingService.cancelExpiredPendingBookings();
        } catch (Exception e) {
            log.warn("Booking expiry scheduler failed", e);
        }
    }
}
