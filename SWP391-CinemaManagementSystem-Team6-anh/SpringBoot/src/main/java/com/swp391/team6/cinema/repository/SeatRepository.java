package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    List<Seat> findByRoomRoomId(Long roomId);
    
    List<Seat> findBySeatType(Seat.SeatType seatType);
}
