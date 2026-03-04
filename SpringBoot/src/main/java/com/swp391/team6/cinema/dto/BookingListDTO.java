package com.swp391.team6.cinema.dto;

import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingListDTO {
    private Long bookingId;
    private LocalDateTime bookingTime;
    private Booking.BookingStatus status;
    private Booking.BookingType bookingType;
    private BigDecimal totalAmount;
    private String customerName;
    private String customerEmail;
    private String movieTitle;
    private Long movieId;
    private LocalDateTime showtimeStart;
    private LocalDateTime showtimeEnd;
    private String roomName;
    private String branchName;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus paymentStatus;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentTime;
}
