package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Room;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final RoomService roomService;

    public List<Seat> getSeatsByRoomOrGenerate(Long roomId, int totalSeats) {

        List<Seat> existingSeats =
                seatRepository.findByRoomRoomIdOrderBySeatRowAscSeatNumberAsc(roomId);

        if (!existingSeats.isEmpty()) {
            return existingSeats;
        }

        Room room = roomService.getRoomById(roomId);

        int seatsPerRow = 10;
        int totalRows = (int) Math.ceil((double) totalSeats / seatsPerRow);

        List<Seat> generatedSeats = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < totalRows; rowIndex++) {

            char rowChar = (char) ('A' + rowIndex);

            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {

                int seatIndex = rowIndex * seatsPerRow + seatNum;
                if (seatIndex > totalSeats) break;

                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setSeatRow(String.valueOf(rowChar));
                seat.setSeatNumber(seatNum);

                // phân loại ghế
                Seat.SeatType seatType = Seat.SeatType.NORMAL;

                if (totalSeats > 50) {

                    if (rowIndex == totalRows - 1) {
                        seatType = Seat.SeatType.COUPLE;
                    }

                    else if (rowIndex >= totalRows - 4) {
                        seatType = Seat.SeatType.VIP;
                    }

                }

                seat.setSeatType(seatType);

                generatedSeats.add(seat);
            }
        }

        return seatRepository.saveAll(generatedSeats);
    }
}