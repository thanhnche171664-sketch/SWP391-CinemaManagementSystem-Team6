package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.BookingDetailDTO;
import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffBookingService {
    private final StaffMovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;
    private final SeatRepository seatRepository;

    public List<Movie> getShowingMovies() {
        return movieRepository.findByStatus(Movie.MovieStatus.now_showing);
    }

    public List<Showtime> getShowtimes(Long movieId, Long branchId) {
        if (movieId == null || branchId == null) return Collections.emptyList();
        return showtimeRepository.findByMovieMovieIdAndRoomBranchBranchIdAndStartTimeAfter(
                movieId, branchId, LocalDateTime.now());
    }

    public Showtime findShowtimeById(Long showtimeId) {
        return showtimeRepository.findByIdWithDetails(showtimeId)
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại"));
    }

    public List<Long> getOccupiedSeatIds(Long showtimeId) {
        return bookingSeatRepository.findByBookingShowtimeId(showtimeId).stream()
                .map(bs -> bs.getSeat().getSeatId()).collect(Collectors.toList());
    }

    // Phương thức này dùng cho trang booking-confirmation.html
    public BookingDetailDTO prepareBookingConfirmationDTO(Long showtimeId, List<Long> seatIds) {
        Showtime showtime = findShowtimeById(showtimeId);
        BookingDetailDTO dto = new BookingDetailDTO();

        dto.setMovieTitle(showtime.getMovie().getTitle());
        dto.setShowtimeStart(showtime.getStartTime());
        dto.setRoomName(showtime.getRoom().getRoomName());
        dto.setBranchName(showtime.getRoom().getBranch().getBranchName());

        BigDecimal seatPrice = new BigDecimal("80000");
        dto.setTotalAmount(seatPrice.multiply(new BigDecimal(seatIds.size())));

        List<String> seatNames = seatRepository.findAllById(seatIds).stream()
                .map(s -> s.getSeatRow() + s.getSeatNumber())
                .collect(Collectors.toList());
        dto.setSeats(seatNames);

        return dto;
    }

    // Ánh xạ Entity sang DTO
    public BookingDetailDTO mapToDTO(Booking booking) {
        BookingDetailDTO dto = new BookingDetailDTO();
        dto.setBookingId(booking.getBookingId());
        dto.setBookingTime(booking.getBookingTime());
        dto.setStatus(booking.getStatus());
        dto.setBookingType(booking.getBookingType());
        dto.setTotalAmount(booking.getTotalAmount());

        if (booking.getUser() != null) {
            dto.setCustomerName(booking.getUser().getFullName());
            dto.setCustomerEmail(booking.getUser().getEmail());
            dto.setCustomerPhone(booking.getUser().getPhone());
        }

        if (booking.getShowtime() != null) {
            dto.setMovieTitle(booking.getShowtime().getMovie().getTitle());
            dto.setShowtimeStart(booking.getShowtime().getStartTime());
            dto.setRoomName(booking.getShowtime().getRoom().getRoomName());
            dto.setBranchName(booking.getShowtime().getRoom().getBranch().getBranchName());
        }

        List<String> seatNames = bookingSeatRepository.findByBooking(booking).stream()
                .map(bs -> bs.getSeat().getSeatRow() + bs.getSeat().getSeatNumber())
                .collect(Collectors.toList());
        dto.setSeats(seatNames);

        return dto;
    }

    @Transactional
    public Booking createStaffBooking(Long showtimeId, List<Long> seatIds, User staff, String method) {
        Showtime showtime = findShowtimeById(showtimeId);
        BigDecimal totalAmount = new BigDecimal("80000").multiply(new BigDecimal(seatIds.size()));

        Booking booking = new Booking();
        booking.setUser(staff);
        booking.setShowtime(showtime);
        booking.setStatus(Booking.BookingStatus.paid);
        booking.setBookingType(Booking.BookingType.counter);
        booking.setTotalAmount(totalAmount);
        booking.setBookingTime(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking);

        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId).orElseThrow();
            BookingSeat bs = new BookingSeat();
            bs.setBooking(savedBooking);
            bs.setSeat(seat);
            bookingSeatRepository.save(bs);
        }

        Payment payment = new Payment();
        payment.setBooking(savedBooking);
        payment.setAmount(totalAmount);
        payment.setMethod(Payment.PaymentMethod.valueOf(method.toUpperCase()));
        payment.setPaymentStatus(Payment.PaymentStatus.success);
        payment.setPaymentTime(LocalDateTime.now());
        paymentRepository.save(payment);

        return savedBooking;
    }
}