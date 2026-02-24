package com.swp391.team6.cinema.dto.booking;

import com.swp391.team6.cinema.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingSummaryDto {

    private Long bookingId;
    private String movieTitle;
    private String branchName;
    private String roomName;
    private LocalDateTime showtimeStartTime;
    private List<String> seatLabels;
    private BigDecimal totalAmount;
    private Booking.BookingStatus status;
}

