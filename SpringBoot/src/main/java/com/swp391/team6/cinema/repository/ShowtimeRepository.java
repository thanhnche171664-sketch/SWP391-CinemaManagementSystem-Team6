package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Query("""
        SELECT s FROM Showtime s
        WHERE s.startTime BETWEEN :start AND :end
        ORDER BY s.startTime ASC
    """)
    List<Showtime> findByDateRange(LocalDateTime start, LocalDateTime end);

    // Check trùng giờ phòng
    @Query("""
        SELECT s FROM Showtime s
        WHERE s.room.roomId = :roomId
        AND (:start < s.endTime AND :end > s.startTime)
    """)
    List<Showtime> checkOverlap(
            Long roomId,
            LocalDateTime start,
            LocalDateTime end
    );

    // (Optional) lấy tất cả có sort
    List<Showtime> findAllByOrderByStartTimeAsc();

    @Query("""
    SELECT s FROM Showtime s
    WHERE s.startTime BETWEEN :start AND :end
    AND s.room.branch.branchId = :branchId
""")
    List<Showtime> findByDateAndBranch(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("branchId") Long branchId
    );

    @Query("SELECT s FROM Showtime s WHERE s.room.branch.branchId = :branchId")
    List<Showtime> findByBranchId(@Param("branchId") Long branchId);

    List<Showtime> findByRoom_RoomId(Long roomId);
    @Query("SELECT s FROM Showtime s JOIN FETCH s.movie JOIN FETCH s.room r JOIN FETCH r.branch WHERE s.status = 'open' AND s.startTime >= :now ORDER BY s.startTime")
    List<Showtime> findAllOpenFromNow(@Param("now") LocalDateTime now);


    Page<Showtime> findAll(Pageable pageable);

    Page<Showtime> findByRoom_Branch_BranchId(Long branchId, Pageable pageable);

    Page<Showtime> findByStartTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Showtime> findByRoom_Branch_BranchIdAndStartTimeBetween(
            Long branchId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}