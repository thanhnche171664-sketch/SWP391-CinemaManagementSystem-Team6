package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.BookingDetailDTO;
import com.swp391.team6.cinema.dto.BookingListDTO;
import com.swp391.team6.cinema.dto.BookingPaymentDTO;
import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.BookingSeat;
import com.swp391.team6.cinema.entity.Payment;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.entity.Showtime;
import com.swp391.team6.cinema.repository.BookingRepository;
import com.swp391.team6.cinema.repository.BookingSeatRepository;
import com.swp391.team6.cinema.repository.PaymentRepository;
import com.swp391.team6.cinema.repository.SeatRepository;
import com.swp391.team6.cinema.repository.ShowtimeRepository;
import com.swp391.team6.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final PricingService pricingService;
    private final PayOSService payOSService;

    @Value("${booking.payment-expiry-minutes:10}")
    private int paymentExpiryMinutes;

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

    @Transactional(readOnly = true)
    public List<BookingListDTO> getBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserUserIdWithDetailsOrderByBookingTimeDesc(userId);
        return mapToListDTO(bookings);
    }

    /** Seat IDs that are already taken for this showtime (pending or paid bookings). */
    @Transactional(readOnly = true)
    public Set<Long> getOccupiedSeatIdsForShowtime(Long showtimeId) {
        List<Booking> bookings = bookingRepository.findByShowtimeShowtimeId(showtimeId).stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.pending || b.getStatus() == Booking.BookingStatus.paid)
                .toList();
        Set<Long> occupied = new HashSet<>();
        for (Booking b : bookings) {
            List<BookingSeat> seats = bookingSeatRepository.findByBookingBookingId(b.getBookingId());
            for (BookingSeat bs : seats) {
                occupied.add(bs.getSeat().getSeatId());
            }
        }
        return occupied;
    }

    /**
     * Create a pending booking with seats and a pending payment record. Returns the booking with id set.
     * Caller should use booking.getBookingId() as orderCode for PayOS and set payment.orderCode, paymentLinkId.
     */
    @Transactional
    public Booking createBooking(Long userId, Long showtimeId, List<Long> seatIds) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));
        if (showtime.getStatus() != Showtime.ShowtimeStatus.open) {
            throw new IllegalArgumentException("Showtime is not available for booking");
        }
        Long roomId = showtime.getRoom().getRoomId();
        Long branchId = showtime.getRoom().getBranch().getBranchId();
        List<Seat> seats = seatRepository.findByRoomRoomIdOrderBySeatRowAscSeatNumberAsc(roomId);
        Set<Long> validSeatIds = seats.stream().map(Seat::getSeatId).collect(Collectors.toSet());
        Set<Long> occupied = getOccupiedSeatIdsForShowtime(showtimeId);
        BigDecimal total = BigDecimal.ZERO;
        List<Seat> selectedSeats = new ArrayList<>();
        for (Long sid : seatIds) {
            if (!validSeatIds.contains(sid)) throw new IllegalArgumentException("Invalid seat id: " + sid);
            if (occupied.contains(sid)) throw new IllegalArgumentException("Seat already taken: " + sid);
            Seat seat = seats.stream().filter(s -> s.getSeatId().equals(sid)).findFirst().orElseThrow();
            selectedSeats.add(seat);
            total = total.add(pricingService.getPrice(branchId, seat.getSeatType(), showtime.getStartTime()));
        }
        if (selectedSeats.isEmpty()) throw new IllegalArgumentException("Select at least one seat");

        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setUser(userRepository.getReferenceById(userId));
        booking.setBookingType(Booking.BookingType.online);
        booking.setStatus(Booking.BookingStatus.pending);
        booking.setTotalAmount(total);
        booking = bookingRepository.save(booking);

        for (Seat seat : selectedSeats) {
            BookingSeat bs = new BookingSeat();
            bs.setBooking(booking);
            bs.setSeat(seat);
            bookingSeatRepository.save(bs);
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setMethod(Payment.PaymentMethod.online);
        payment.setPaymentStatus(Payment.PaymentStatus.pending);
        payment.setAmount(total);
        payment.setOrderCode(booking.getBookingId());
        paymentRepository.save(payment);

        return booking;
    }

    /**
     * If booking is still pending, try to sync payment status from PayOS (e.g. when user returns to success URL
     * and webhook was not received). Updates Payment and Booking if PayOS reports paid.
     */
    @Transactional
    public void syncPaymentStatusFromPayOS(Long bookingId) {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderCode(bookingId);
        if (paymentOpt.isEmpty()) return;
        Payment payment = paymentOpt.get();
        if (payment.getPaymentStatus() != Payment.PaymentStatus.pending) return;
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty() || bookingOpt.get().getStatus() != Booking.BookingStatus.pending) return;
        var statusOpt = payOSService.getPaymentStatusByOrderCode(bookingId);
        if (statusOpt.isEmpty()) return;
        Booking booking = bookingOpt.get();
        if (Boolean.TRUE.equals(statusOpt.get())) {
            payment.setPaymentStatus(Payment.PaymentStatus.success);
            paymentRepository.save(payment);
            booking.setStatus(Booking.BookingStatus.paid);
            bookingRepository.save(booking);
        } else {
            payment.setPaymentStatus(Payment.PaymentStatus.failed);
            paymentRepository.save(payment);
        }
    }

    /**
     * Cancel a pending booking (e.g. user cancelled on PayOS). Sets payment to cancelled and booking to cancelled.
     */
    @Transactional
    public void cancelPendingBooking(Long bookingId, Long userId) {
        Optional<Booking> opt = bookingRepository.findById(bookingId);
        if (opt.isEmpty()) return;
        Booking booking = opt.get();
        if (!booking.getUser().getUserId().equals(userId)) return;
        if (booking.getStatus() != Booking.BookingStatus.pending) return;
        cancelPendingBookingInternal(booking);
    }

    /**
     * Cancel all pending bookings whose bookingTime is older than paymentExpiryMinutes. Releases seats.
     */
    @Transactional
    public void cancelExpiredPendingBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(paymentExpiryMinutes);
        List<Booking> expired = bookingRepository.findByStatusAndBookingTimeBefore(Booking.BookingStatus.pending, cutoff);
        for (Booking booking : expired) {
            cancelPendingBookingInternal(booking);
        }
    }

    private void cancelPendingBookingInternal(Booking booking) {
        if (booking.getStatus() != Booking.BookingStatus.pending) return;
        Long bookingId = booking.getBookingId();
        List<Payment> payments = paymentRepository.findByBookingBookingIdOrderByPaymentTimeDesc(bookingId);
        for (Payment p : payments) {
            if (p.getPaymentStatus() == Payment.PaymentStatus.pending) {
                p.setPaymentStatus(Payment.PaymentStatus.cancelled);
                paymentRepository.save(p);
            }
        }
        booking.setStatus(Booking.BookingStatus.cancelled);
        bookingRepository.save(booking);
    }

    /** Update payment with PayOS link id (after creating link). */
    @Transactional
    public void setPaymentLinkId(Long bookingId, String paymentLinkId) {
        List<Payment> payments = paymentRepository.findByBookingBookingIdOrderByPaymentTimeDesc(bookingId);
        if (!payments.isEmpty()) {
            Payment p = payments.get(0);
            p.setPaymentLinkId(paymentLinkId);
            paymentRepository.save(p);
        }
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
