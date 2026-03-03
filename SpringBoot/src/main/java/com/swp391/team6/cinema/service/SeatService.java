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

        //Nếu đã có ghế → trả về luôn
        if (!existingSeats.isEmpty()) {
            return existingSeats;
        }

        Room room = roomService.getRoomById(roomId);

        int seatsPerRow = 10;
        int totalRows = (int) Math.ceil((double) totalSeats / seatsPerRow);

        List<Seat> generatedSeats = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < totalRows; rowIndex++) {
            char rowChar = (char) ('A' + rowIndex); // A, B, C...

            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {

                int seatIndex = rowIndex * seatsPerRow + seatNum;
                if (seatIndex > totalSeats) break;

                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setSeatRow(String.valueOf(rowChar));
                seat.setSeatNumber(seatNum);
                seat.setSeatType(Seat.SeatType.NORMAL);

                generatedSeats.add(seat);
            }
        }

        return seatRepository.saveAll(generatedSeats);
    }
}