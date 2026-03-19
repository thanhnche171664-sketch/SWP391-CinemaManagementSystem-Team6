package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    List<BookingSeat> findByBookingBookingId(Long bookingId);

    List<BookingSeat> findByBooking(Booking booking);

    List<BookingSeat> findBySeatSeatId(Long seatId);

}