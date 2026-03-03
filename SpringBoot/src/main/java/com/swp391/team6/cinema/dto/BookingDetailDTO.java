package com.swp391.team6.cinema.dto;

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
public class BookingDetailDTO {
    private Long bookingId;
    private LocalDateTime bookingTime;
    private Booking.BookingStatus status;
    private Booking.BookingType bookingType;
    private BigDecimal totalAmount;

    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private String movieTitle;
    private LocalDateTime showtimeStart;
    private LocalDateTime showtimeEnd;
    private String roomName;
    private String branchName;

    private List<String> seats;
    private List<BookingPaymentDTO> payments;
}
