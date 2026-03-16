package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StaffBookingService {
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PromotionRepository promotionRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final PricingService pricingService;
    private final BookingService bookingService;
    private final SeatLockService seatLockService;

    @Transactional
    public Booking processCounterBooking(User staff, Long showtimeId, List<Long> seatIds,
                                         String phone, String email, String paymentMethodStr,
                                         String promoCode) {

        if (!"cash".equalsIgnoreCase(paymentMethodStr)) {
            throw new IllegalArgumentException("Hệ thống chỉ hỗ trợ thanh toán tiền mặt (cash) tại quầy");
        }

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại"));

        User customer = null;
        if (email != null && !email.isBlank()) {
            customer = userRepository.findByEmail(email).orElse(null);
        } else if (phone != null && !phone.isBlank()) {
            customer = userRepository.findByPhone(phone).orElse(null);
        }

        if (customer == null) {
            customer = userRepository.findByFullName("GUEST")
                    .orElseThrow(() -> new IllegalArgumentException("Hệ thống chưa cấu hình tài khoản 'GUEST'"));
        }

        Long branchId = showtime.getRoom().getBranch().getBranchId();
        List<Seat> allSeats = seatRepository.findByRoomRoomIdOrderBySeatRowAscSeatNumberAsc(showtime.getRoom().getRoomId());
        Set<Long> occupied = bookingService.getOccupiedSeatIdsForShowtime(showtimeId);

        BigDecimal total = BigDecimal.ZERO;
        List<Seat> selectedSeats = new ArrayList<>();

        for (Long sid : seatIds) {
            Seat seat = allSeats.stream().filter(s -> s.getSeatId().equals(sid)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Ghế ID " + sid + " không tồn tại"));
            if (occupied.contains(sid)) throw new IllegalArgumentException("Ghế đã được đặt");

            selectedSeats.add(seat);
            total = total.add(pricingService.getPrice(branchId, seat.getSeatType(), showtime.getStartTime()));
        }

        Promotion appliedPromo = null;
        if (promoCode != null && !promoCode.isBlank()) {
            appliedPromo = promotionRepository.findByPromoCode(promoCode);

            if (appliedPromo == null || appliedPromo.getStatus() != Promotion.Status.active) {
                throw new IllegalArgumentException("Mã giảm giá không tồn tại hoặc không khả dụng");
            }
            if (total.compareTo(appliedPromo.getMinBookingAmount()) < 0) {
                throw new IllegalArgumentException("Đơn hàng không đủ giá trị tối thiểu để áp dụng mã này");
            }

            BigDecimal discount = BigDecimal.ZERO;
            if (appliedPromo.getDiscountType() == Promotion.DiscountType.percent) {
                discount = total.multiply(appliedPromo.getDiscountValue())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            } else {
                discount = appliedPromo.getDiscountValue();
            }

            total = total.subtract(discount).max(BigDecimal.ZERO);

            appliedPromo.setUsedCount(appliedPromo.getUsedCount() + 1);
            promotionRepository.save(appliedPromo);
        }

        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setUser(customer);
        booking.setBookingType(Booking.BookingType.counter);
        booking.setStatus(Booking.BookingStatus.paid);
        booking.setTotalAmount(total);

        if (appliedPromo != null) {
            booking.setPromotion(appliedPromo);
        }

        booking = bookingRepository.save(booking);

        for (Seat seat : selectedSeats) {
            BookingSeat bs = new BookingSeat();
            bs.setBooking(booking);
            bs.setSeat(seat);
            bookingSeatRepository.save(bs);
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setMethod(Payment.PaymentMethod.cash);
        payment.setAmount(total);
        payment.setPaymentStatus(Payment.PaymentStatus.success);
        paymentRepository.save(payment);

        for (Long sid : seatIds) {
            seatLockService.releaseSeat(showtimeId, sid);
        }

        return booking;
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé"));

        if (booking.getStatus() == Booking.BookingStatus.cancelled) {
            throw new IllegalArgumentException("Vé này đã bị hủy trước đó");
        }

        if (booking.getPromotion() != null) {
            Promotion promo = booking.getPromotion();
            promo.setUsedCount(Math.max(0, promo.getUsedCount() - 1));
            promotionRepository.save(promo);
        }

        booking.setStatus(Booking.BookingStatus.cancelled);
        bookingRepository.save(booking);

        Payment payment = paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin thanh toán"));
        payment.setPaymentStatus(Payment.PaymentStatus.failed);
        paymentRepository.save(payment);
    }
}