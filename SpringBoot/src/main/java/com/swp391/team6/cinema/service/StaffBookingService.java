package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffBookingService {
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;
    private final PricingService pricingService;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingService bookingService;

    @Transactional
    public Booking processCounterBooking(User staff, Long showtimeId, List<Long> seatIds,
                                         String phone, String email, String paymentMethodStr) {

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại"));

        User customer = null;
        if (email != null && !email.isBlank()) {
            customer = userRepository.findByEmail(email).orElse(null);
        }
        if (customer == null && phone != null && !phone.isBlank()) {
            customer = userRepository.findByPhone(phone).orElse(null);
        }

        if (customer == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng với thông tin: " + (email.isBlank() ? phone : email));
        }

        if (showtime.getStatus() != Showtime.ShowtimeStatus.open) {
            throw new IllegalArgumentException("Suất chiếu hiện không khả dụng để đặt vé");
        }


        Long roomId = showtime.getRoom().getRoomId();
        Long branchId = showtime.getRoom().getBranch().getBranchId();

        List<Seat> validSeatsInRoom = seatRepository.findByRoomRoomIdOrderBySeatRowAscSeatNumberAsc(roomId);
        Set<Long> validSeatIds = validSeatsInRoom.stream().map(Seat::getSeatId).collect(Collectors.toSet());
        Set<Long> occupied = bookingService.getOccupiedSeatIdsForShowtime(showtimeId);

        BigDecimal total = BigDecimal.ZERO;
        List<Seat> selectedSeats = new ArrayList<>();

        for (Long sid : seatIds) {
            if (!validSeatIds.contains(sid)) {
                throw new IllegalArgumentException("Ghế ID " + sid + " không thuộc phòng chiếu này");
            }
            if (occupied.contains(sid)) {
                throw new IllegalArgumentException("Ghế ID " + sid + " đã bị đặt");
            }

            Seat seat = validSeatsInRoom.stream().filter(s -> s.getSeatId().equals(sid)).findFirst().get();
            selectedSeats.add(seat);
            total = total.add(pricingService.getPrice(branchId, seat.getSeatType(), showtime.getStartTime()));
        }

        if (selectedSeats.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ít nhất một ghế");

        // 4. Tạo Booking
        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setUser(customer);
        booking.setBookingType(Booking.BookingType.counter);

        Payment.PaymentMethod method;
        try {
            method = Payment.PaymentMethod.valueOf(paymentMethodStr.toLowerCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ: " + paymentMethodStr);
        }
        if (method == Payment.PaymentMethod.online) {
            booking.setStatus(Booking.BookingStatus.pending);
        } else {
            booking.setStatus(Booking.BookingStatus.paid);
        }

        booking.setTotalAmount(total);
        booking = bookingRepository.save(booking);

        // 5. Lưu chi tiết ghế
        for (Seat seat : selectedSeats) {
            BookingSeat bs = new BookingSeat();
            bs.setBooking(booking);
            bs.setSeat(seat);
            bookingSeatRepository.save(bs);
        }

        // 6. Lưu thông tin thanh toán
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setMethod(method);
        payment.setAmount(total);
        payment.setOrderCode(booking.getBookingId());

        if (method == Payment.PaymentMethod.online) {
            payment.setPaymentStatus(Payment.PaymentStatus.pending);
        } else {
            payment.setPaymentStatus(Payment.PaymentStatus.success);
        }

        paymentRepository.save(payment);

        return booking;
    }
}