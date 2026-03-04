package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class BookingService {

    private final ShowtimeRepository showtimeRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatRepository seatRepository;
    private final PricingRepository pricingRepository;
    private final PaymentRepository paymentRepository;
    private final PayOSService payOSService;

    public BookingService(ShowtimeRepository showtimeRepository,
                          BookingRepository bookingRepository,
                          BookingSeatRepository bookingSeatRepository,
                          SeatRepository seatRepository,
                          PricingRepository pricingRepository,
                          PaymentRepository paymentRepository,
                          PayOSService payOSService) {
        this.showtimeRepository = showtimeRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.seatRepository = seatRepository;
        this.pricingRepository = pricingRepository;
        this.paymentRepository = paymentRepository;
        this.payOSService = payOSService;
    }

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

    /**
     * Returns set of seat IDs that are already taken (pending or paid bookings) for this showtime.
     */
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

    /**
     * Returns all seats for the showtime's room, with availability info.
     */
    public List<Seat> getSeatsWithAvailability(Long showtimeId) {
        Showtime showtime = getShowtimeById(showtimeId);
        Long roomId = showtime.getRoom().getRoomId();
        List<Seat> seats = seatRepository.findByRoomRoomIdOrderBySeatRowAscSeatNumberAsc(roomId);
        Set<Long> occupied = getOccupiedSeatIds(showtimeId);
        // We return all seats; view will use occupied set to mark which are disabled
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

    /**
     * If booking is still pending, check PayOS for payment status and update DB if already paid.
     * Use when user lands on booking-success so they see "Đã thanh toán" even if webhook hasn't run yet.
     */
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
}
