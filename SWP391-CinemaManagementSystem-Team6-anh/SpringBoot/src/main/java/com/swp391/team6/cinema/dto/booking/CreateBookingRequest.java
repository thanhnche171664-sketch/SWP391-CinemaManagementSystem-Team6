package com.swp391.team6.cinema.dto.booking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotNull
    private Long showtimeId;

    @NotEmpty
    private List<Long> seatIds;

    /**
     * Reserved for future extension (e.g. COUNTER vs ONLINE).
     * For now, all PayOS flows will use ONLINE.
     */
    private String bookingType;
}

