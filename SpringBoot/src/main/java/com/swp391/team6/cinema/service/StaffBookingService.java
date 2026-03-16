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
    private final PaymentRepository paymentRepository;
    private final PricingService pricingService;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingService bookingService;
    private final PromotionRepository promotionRepository;

    @Transactional
    public Booking processCounterBooking(User staff, Long showtimeId, List<Long> seatIds,
                                         String phone, String email, String paymentMethodStr,
                                         String promoCode) {

        // 1. Kiểm tra phương thức thanh toán
        if (!"cash".equalsIgnoreCase(paymentMethodStr)) {
            throw new IllegalArgumentException("Hệ thống chỉ hỗ trợ thanh toán tiền mặt (cash) tại quầy");
        }

        // 2. Tìm suất chiếu và khách hàng
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

        // 3. Tính tiền gốc
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

        // 4. Xử lý Promotion (Nếu có)
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
                // Tính % giảm, làm tròn 2 chữ số thập phân
                discount = total.multiply(appliedPromo.getDiscountValue())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            } else {
                discount = appliedPromo.getDiscountValue();
            }

            total = total.subtract(discount).max(BigDecimal.ZERO);

            // Tăng số lượt đã sử dụng
            appliedPromo.setUsedCount(appliedPromo.getUsedCount() + 1);
            promotionRepository.save(appliedPromo);
        }

        // 5. Tạo Booking
        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setUser(customer);
        booking.setBookingType(Booking.BookingType.counter);
        booking.setStatus(Booking.BookingStatus.paid);
        booking.setTotalAmount(total);

        // Gán promotion vào booking để quản lý doanh thu
        if (appliedPromo != null) {
            booking.setPromotion(appliedPromo);
        }

        booking = bookingRepository.save(booking);

        // 6. Lưu chi tiết ghế
        for (Seat seat : selectedSeats) {
            BookingSeat bs = new BookingSeat();
            bs.setBooking(booking);
            bs.setSeat(seat);
            bookingSeatRepository.save(bs);
        }

        // 7. Lưu thanh toán
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setMethod(Payment.PaymentMethod.cash);
        payment.setAmount(total);
        payment.setPaymentStatus(Payment.PaymentStatus.success);
        paymentRepository.save(payment);

        return booking;
    }
}