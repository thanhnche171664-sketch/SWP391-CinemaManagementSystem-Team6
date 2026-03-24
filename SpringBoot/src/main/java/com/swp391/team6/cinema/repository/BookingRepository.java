package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserUserId(Long userId);

    List<Booking> findByUserUserIdOrderByBookingTimeDesc(Long userId);

    @Query("""
            select b from Booking b
            join fetch b.user u
            join fetch b.showtime s
            join fetch s.movie m
            join fetch s.room r
            join fetch r.branch br
            where u.userId = :userId
            order by b.bookingTime desc
            """)
    List<Booking> findByUserUserIdWithDetailsOrderByBookingTimeDesc(@Param("userId") Long userId);

    List<Booking> findByShowtimeShowtimeId(Long showtimeId);

    List<Booking> findByStatus(Booking.BookingStatus status);

    List<Booking> findByStatusAndBookingTimeBefore(Booking.BookingStatus status, LocalDateTime bookingTime);

    @Query("""
            select distinct b from Booking b
            join fetch b.user u
            join fetch b.showtime s
            join fetch s.movie m
            join fetch s.room r
            join fetch r.branch br
            """)
    List<Booking> findAllWithDetails();

    @Query("""
            select distinct b from Booking b
            join fetch b.user u
            join fetch b.showtime s
            join fetch s.movie m
            join fetch s.room r
            join fetch r.branch br
            where br.branchId = :branchId
            """)
    List<Booking> findByBranchIdWithDetails(@Param("branchId") Long branchId);

    @Query("""
            select b from Booking b
            join fetch b.user u
            join fetch b.showtime s
            join fetch s.movie m
            join fetch s.room r
            join fetch r.branch br
            left join fetch b.promotion p
            where b.bookingId = :bookingId
            """)
    Optional<Booking> findByIdWithDetails(@Param("bookingId") Long bookingId);

    @Query("""
            select b from Booking b
            join fetch b.user u
            join fetch b.showtime s
            join fetch s.movie m
            join fetch s.room r
            join fetch r.branch br
            where b.bookingId = :bookingId
              and br.branchId = :branchId
            """)
    Optional<Booking> findByIdAndBranchIdWithDetails(@Param("bookingId") Long bookingId,
                                                     @Param("branchId") Long branchId);

    @Query("""
    SELECT b FROM Booking b 
    JOIN FETCH b.showtime s 
    JOIN FETCH s.room r 
    JOIN FETCH r.branch br 
    WHERE br.branchId = :branchId 
    AND (:name IS NULL OR :name = '' OR b.user.fullName LIKE %:name%)
    ORDER BY b.bookingId DESC
    """)
    Page<Booking> findByBranchIdWithDetails(
            @Param("branchId") Long branchId,
            @Param("name") String name,
            Pageable pageable);

    // Kiểm tra xem User có vé đã xác nhận cho Movie này và suất chiếu đã kết thúc chưa
    // showtime.startTime < :time (Ví dụ: hiện tại - 120 phút)
    boolean existsByUserUserIdAndShowtimeMovieMovieIdAndStatusAndShowtimeStartTimeBefore(
            Long userId,
            Long movieId,
            Booking.BookingStatus status,
            LocalDateTime time
    );
}