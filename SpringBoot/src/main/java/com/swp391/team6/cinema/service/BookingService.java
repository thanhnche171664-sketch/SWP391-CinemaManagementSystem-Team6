package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.BookingDetailDTO;
import com.swp391.team6.cinema.dto.BookingListDTO;
import com.swp391.team6.cinema.dto.BookingPaymentDTO;
import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.BookingSeat;
import com.swp391.team6.cinema.entity.Payment;
import com.swp391.team6.cinema.repository.BookingRepository;
import com.swp391.team6.cinema.repository.BookingSeatRepository;
import com.swp391.team6.cinema.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final BookingSeatRepository bookingSeatRepository;

    @Transactional(readOnly = true)
    public List<BookingListDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAllWithDetails();
        return mapToListDTO(bookings);
    }

    @Transactional(readOnly = true)
    public List<BookingListDTO> getBookingsByBranch(Long branchId) {
        List<Booking> bookings = bookingRepository.findByBranchIdWithDetails(branchId);
        return mapToListDTO(bookings);
    }

    @Transactional(readOnly = true)
    public Optional<BookingDetailDTO> getBookingDetail(Long bookingId) {
        return bookingRepository.findByIdWithDetails(bookingId)
                .map(this::mapToDetailDTO);
    }

    @Transactional(readOnly = true)
    public Optional<BookingDetailDTO> getBookingDetailForBranch(Long bookingId, Long branchId) {
        return bookingRepository.findByIdAndBranchIdWithDetails(bookingId, branchId)
                .map(this::mapToDetailDTO);
    }

    private List<BookingListDTO> mapToListDTO(List<Booking> bookings) {
        List<BookingListDTO> results = new ArrayList<>();
        for (Booking booking : bookings) {
            BookingListDTO dto = new BookingListDTO();
            dto.setBookingId(booking.getBookingId());
            dto.setBookingTime(booking.getBookingTime());
            dto.setStatus(booking.getStatus());
            dto.setBookingType(booking.getBookingType());
            dto.setTotalAmount(booking.getTotalAmount());
            dto.setCustomerName(booking.getUser().getFullName());
            dto.setCustomerEmail(booking.getUser().getEmail());
            dto.setMovieTitle(booking.getShowtime().getMovie().getTitle());
            dto.setShowtimeStart(booking.getShowtime().getStartTime());
            dto.setShowtimeEnd(booking.getShowtime().getEndTime());
            dto.setRoomName(booking.getShowtime().getRoom().getRoomName());
            dto.setBranchName(booking.getShowtime().getRoom().getBranch().getBranchName());

            Payment latestPayment = getLatestPayment(booking.getBookingId());
            if (latestPayment != null) {
                dto.setPaymentMethod(latestPayment.getMethod());
                dto.setPaymentStatus(latestPayment.getPaymentStatus());
                dto.setPaymentAmount(latestPayment.getAmount());
                dto.setPaymentTime(latestPayment.getPaymentTime());
            }
            results.add(dto);
        }
        return results;
    }

    private BookingDetailDTO mapToDetailDTO(Booking booking) {
        BookingDetailDTO dto = new BookingDetailDTO();
        dto.setBookingId(booking.getBookingId());
        dto.setBookingTime(booking.getBookingTime());
        dto.setStatus(booking.getStatus());
        dto.setBookingType(booking.getBookingType());
        dto.setTotalAmount(booking.getTotalAmount());

        dto.setCustomerName(booking.getUser().getFullName());
        dto.setCustomerEmail(booking.getUser().getEmail());
        dto.setCustomerPhone(booking.getUser().getPhone());

        dto.setMovieTitle(booking.getShowtime().getMovie().getTitle());
        dto.setShowtimeStart(booking.getShowtime().getStartTime());
        dto.setShowtimeEnd(booking.getShowtime().getEndTime());
        dto.setRoomName(booking.getShowtime().getRoom().getRoomName());
        dto.setBranchName(booking.getShowtime().getRoom().getBranch().getBranchName());

        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingBookingId(booking.getBookingId());
        List<String> seatNames = bookingSeats.stream()
                .map(seat -> seat.getSeat().getSeatRow() + seat.getSeat().getSeatNumber())
                .collect(Collectors.toList());
        dto.setSeats(seatNames);

        List<Payment> payments = paymentRepository.findByBookingBookingIdOrderByPaymentTimeDesc(booking.getBookingId());
        List<BookingPaymentDTO> paymentDTOs = payments.stream()
                .map(this::toPaymentDTO)
                .collect(Collectors.toList());
        dto.setPayments(paymentDTOs);

        return dto;
    }

    private Payment getLatestPayment(Long bookingId) {
        List<Payment> payments = paymentRepository.findByBookingBookingIdOrderByPaymentTimeDesc(bookingId);
        return payments.isEmpty() ? null : payments.get(0);
    }

    private BookingPaymentDTO toPaymentDTO(Payment payment) {
        return new BookingPaymentDTO(
                payment.getMethod(),
                payment.getPaymentStatus(),
                payment.getAmount(),
                payment.getPaymentTime()
        );
    }
}
