package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    
    List<Showtime> findByMovieMovieId(Long movieId);
    
    List<Showtime> findByRoomRoomId(Long roomId);
    
    @Query("SELECT s FROM Showtime s WHERE s.startTime BETWEEN :startDate AND :endDate")
    List<Showtime> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Showtime> findByStatus(Showtime.ShowtimeStatus status);
}
