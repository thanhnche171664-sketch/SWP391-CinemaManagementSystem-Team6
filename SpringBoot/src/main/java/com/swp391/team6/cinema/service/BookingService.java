package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.BookingDetailDTO;
import com.swp391.team6.cinema.dto.BookingListDTO;
import com.swp391.team6.cinema.dto.BookingPaymentDTO;
import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final ShowtimeRepository showtimeRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatRepository seatRepository;
    private final PricingRepository pricingRepository;
    private final PaymentRepository paymentRepository;
    private final PayOSService payOSService;

    // =========================================================================
    // PHẦN LOGIC ĐẶT VÉ
    // =========================================================================

    public List<Showtime> getShowtimesByMovie(Long movieId) {
        LocalDateTime now = LocalDateTime.now();
        return showtimeRepository.findByMovieMovieId(movieId).stream()
                .filter(s -> s.getStatus() == Showtime.ShowtimeStatus.open
                        && s.getStartTime() != null
                        && !s.getStartTime().isBefore(now))
                .sorted(Comparator.comparing(Showtime::getStartTime))
                .toList();
    }

    public Showtime getShowtimeById(Long showtimeId) {
        return showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu"));
    }

    public Set<Long> getOccupiedSeatIds(Long showtimeId) {
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

    public List<Seat> getSeatsWithAvailability(Long showtimeId) {
        Showtime showtime = getShowtimeById(showtimeId);
        Long roomId = showtime.getRoom().getRoomId();
        List<Seat> seats = seatRepository.findByRoomRoomIdOrderBySeatRowAscSeatNumberAsc(roomId);
        return seats;
    }

    public Set<Long> getAvailableSeatIds(Long showtimeId) {
        Showtime showtime = getShowtimeById(showtimeId);
        Long roomId = showtime.getRoom().getRoomId();
        List<Seat> seats = seatRepository.findByRoomRoomIdOrderBySeatRowAscSeatNumberAsc(roomId);
        Set<Long> allIds = seats.stream().map(Seat::getSeatId).collect(Collectors.toSet());
        Set<Long> occupied = getOccupiedSeatIds(showtimeId);
        allIds.removeAll(occupied);
        return allIds;
    }

    private BigDecimal getPriceForSeat(Showtime showtime, Seat seat) {
        Long branchId = showtime.getRoom().getBranch().getBranchId();
        List<Pricing> list = pricingRepository.findByBranchBranchIdAndSeatType(branchId, seat.getSeatType());
        if (list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return list.get(0).getPrice();
    }

    @Transactional
    public Booking createBooking(Long showtimeId, List<Long> seatIds, User user) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một ghế");
        }
        Showtime showtime = getShowtimeById(showtimeId);
        Set<Long> available = getAvailableSeatIds(showtimeId);
        for (Long seatId : seatIds) {
            if (!available.contains(seatId)) {
                throw new RuntimeException("Ghế đã được đặt hoặc không hợp lệ");
            }
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Seat> seats = new ArrayList<>();
        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế"));
            totalAmount = totalAmount.add(getPriceForSeat(showtime, seat));
            seats.add(seat);
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setStatus(Booking.BookingStatus.pending);
        booking.setTotalAmount(totalAmount);
        booking = bookingRepository.save(booking);

        for (Seat seat : seats) {
            BookingSeat bs = new BookingSeat();
            bs.setBooking(booking);
            bs.setSeat(seat);
            bookingSeatRepository.save(bs);
        }
        return booking;
    }

    @Transactional
    public void confirmPaymentByOrderCode(Long orderCode) {
        Payment payment = paymentRepository.findByExternalOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Payment not found for orderCode: " + orderCode));
        payment.setPaymentStatus(Payment.PaymentStatus.success);
        paymentRepository.save(payment);
        Booking booking = payment.getBooking();
        booking.setStatus(Booking.BookingStatus.paid);
        bookingRepository.save(booking);
    }

    public void syncPaymentStatusFromPayOSIfPending(Long bookingId) {
        Optional<Booking> opt = bookingRepository.findById(bookingId);
        if (opt.isEmpty() || opt.get().getStatus() != Booking.BookingStatus.pending) return;
        List<Payment> payments = paymentRepository.findByBookingBookingId(bookingId);
        Optional<Long> orderCode = payments.stream()
                .map(Payment::getExternalOrderCode)
                .filter(Objects::nonNull)
                .findFirst();
        if (orderCode.isEmpty()) return;
        try {
            if (payOSService.isPaymentCompletedByOrderCode(orderCode.get())) {
                confirmPaymentByOrderCode(orderCode.get());
            }
        } catch (Exception ignored) {
        }
    }

    public Optional<Booking> getBookingByIdAndUser(Long bookingId, Long userId) {
        return bookingRepository.findById(bookingId)
                .filter(b -> b.getUser().getUserId().equals(userId));
    }

    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserUserId(userId);
    }

    // =========================================================================
    // PHẦN LOGIC QUẢN LÝ / ADMIN
    // =========================================================================

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