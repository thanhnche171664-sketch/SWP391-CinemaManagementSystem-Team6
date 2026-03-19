package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByBookingBookingId(Long bookingId);
    
    List<Payment> findByBookingBookingIdOrderByPaymentTimeDesc(Long bookingId);

    List<Payment> findByPaymentStatus(Payment.PaymentStatus paymentStatus);

    java.util.Optional<Payment> findByOrderCode(Long orderCode);

    Optional<Payment> findByBooking(Booking booking);
}
