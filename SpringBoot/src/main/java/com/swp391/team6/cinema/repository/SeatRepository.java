package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Room;
import com.swp391.team6.cinema.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    List<Seat> findByRoomRoomId(Long roomId);
    
    List<Seat> findBySeatType(Seat.SeatType seatType);

    List<Seat> findByRoomRoomIdOrderBySeatRowAscSeatNumberAsc(Long roomId);
    //SELECT *
    //FROM seat s
    //WHERE s.room_id = ?
    //ORDER BY s.seat_row ASC, s.seat_number ASC;

    List<Seat> findByRoom(Room room);

    void deleteByRoom(Room room);
}
