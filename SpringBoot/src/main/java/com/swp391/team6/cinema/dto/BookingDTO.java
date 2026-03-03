package com.swp391.team6.cinema.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private Long booking_id;
    private String movie_title;
    private String branch_name;
    private String seat_names;
    private BigDecimal total_amount;
    private String status;
    private LocalDateTime booking_time;
}