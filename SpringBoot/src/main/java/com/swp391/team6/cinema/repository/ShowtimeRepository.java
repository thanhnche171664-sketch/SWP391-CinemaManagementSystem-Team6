package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieMovieId(Long movieId);

    List<Showtime> findByRoomRoomId(Long roomId);

    @Query("SELECT s FROM Showtime s WHERE s.startTime BETWEEN :startDate AND :endDate")
    List<Showtime> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Showtime> findByStatus(Showtime.ShowtimeStatus status);

    @Query("SELECT s FROM Showtime s JOIN FETCH s.room r JOIN FETCH r.branch WHERE s.movie.movieId = :movieId AND s.status = 'open' AND s.startTime > :after ORDER BY s.startTime")
    List<Showtime> findByMovieIdOpenAfterWithRoomAndBranch(@Param("movieId") Long movieId, @Param("after") LocalDateTime after);

    @Query("SELECT s FROM Showtime s JOIN FETCH s.movie JOIN FETCH s.room r JOIN FETCH r.branch WHERE s.showtimeId = :id")
    java.util.Optional<Showtime> findByIdWithMovieRoomBranch(@Param("id") Long id);

    @Query("SELECT s FROM Showtime s JOIN s.room r WHERE s.movie.movieId = :movieId AND r.branch.branchId = :branchId")
    List<Showtime> findByMovieAndBranch(@Param("movieId") Long movieId, @Param("branchId") Long branchId);

}