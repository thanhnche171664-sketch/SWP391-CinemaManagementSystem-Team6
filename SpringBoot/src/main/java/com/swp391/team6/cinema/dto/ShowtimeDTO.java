package com.swp391.team6.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeDTO {
    private Long showtimeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String roomName;
    private String movieTitle;
}