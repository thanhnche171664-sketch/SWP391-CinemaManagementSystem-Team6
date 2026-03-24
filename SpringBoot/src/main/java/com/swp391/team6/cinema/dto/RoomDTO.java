package com.swp391.team6.cinema.dto;

public class RoomDTO {
    private Long roomId;
    private String roomName;
    private int totalSeats;

    private Long branchId;
    private String branchName;

    public RoomDTO(Long roomId, String roomName) {
    }

    public class SeatUpdateRequest {
        private Long seatId;
        private String type;

    }
}
