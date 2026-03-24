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
public class CounterBookingService {
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
    private final PayOSService payOSService;

    @Transactional
    public Booking processCounterBooking(User staff, Long showtimeId, List<Long> seatIds,
                                         String phone, String email, String paymentMethodStr,
                                         String promoCode) {

        boolean isPayOS = "payos".equalsIgnoreCase(paymentMethodStr) || "online".equalsIgnoreCase(paymentMethodStr);
        boolean isCash = "cash".equalsIgnoreCase(paymentMethodStr);

        if (!isPayOS && !isCash) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ (Chỉ hỗ trợ cash hoặc payos)");
        }

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại"));

        User customer = null;
        if (email != null && !email.trim().isEmpty()) {
            customer = userRepository.findByEmail(email.trim()).orElse(null);
        } else if (phone != null && !phone.trim().isEmpty()) {
            customer = userRepository.findByPhone(phone.trim()).orElse(null);
        }

        if (customer == null) {
            customer = userRepository.findFirstByRole(User.UserRole.CUSTOMER)
                    .orElseThrow(() -> new IllegalArgumentException("Hệ thống chưa cấu hình tài khoản mặc định cho Role CUSTOMER"));
        }

        Long branchId = showtime.getRoom().getBranch().getBranchId();
        List<Seat> allSeats = seatRepository.findByRoomRoomIdOrderBySeatRowAscSeatNumberAsc(showtime.getRoom().getRoomId());
        Set<Long> occupied = bookingService.getOccupiedSeatIdsForShowtime(showtimeId);

        BigDecimal total = BigDecimal.ZERO;
        List<Seat> selectedSeats = new ArrayList<>();

        for (Long sid : seatIds) {
            Seat seat = allSeats.stream().filter(s -> s.getSeatId().equals(sid)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Ghế ID " + sid + " không tồn tại"));
            if (occupied.contains(sid)) throw new IllegalArgumentException("Ghế đã được đặt hoặc đang được giữ");

            selectedSeats.add(seat);
            total = total.add(pricingService.getPrice(branchId, seat.getSeatType(), showtime.getStartTime()));
        }

        Promotion appliedPromo = null;
        if (promoCode != null && !promoCode.isBlank()) {
            appliedPromo = promotionRepository.findByPromoCode(promoCode).orElse(null);

            if (appliedPromo == null || appliedPromo.getStatus() != Promotion.Status.active) {
                throw new IllegalArgumentException("Mã giảm giá không tồn tại hoặc không khả dụng");
            }
            if (appliedPromo.getBranch() == null) {
                throw new IllegalArgumentException("Mã giảm giá này chưa được kích hoạt cho bất kỳ chi nhánh nào");
            }
            if (!appliedPromo.getBranch().getBranchId().equals(branchId)) {
                throw new IllegalArgumentException("Mã giảm giá này không áp dụng cho chi nhánh này");
            }
            if (total.compareTo(appliedPromo.getMinBookingAmount()) < 0) {
                throw new IllegalArgumentException("Đơn hàng không đủ giá trị tối thiểu để áp dụng mã này");
            }
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (appliedPromo.getStartDate() != null && now.isBefore(appliedPromo.getStartDate())) {
                throw new IllegalArgumentException("Chương trình khuyến mãi chưa bắt đầu");
            }
            if (appliedPromo.getEndDate() != null && now.isAfter(appliedPromo.getEndDate())) {
                throw new IllegalArgumentException("Mã giảm giá đã hết hạn");
            }
            if (appliedPromo.getUsageLimit() != null && appliedPromo.getUsedCount() >= appliedPromo.getUsageLimit()) {
                throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng");
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

        booking.setStatus(isPayOS ? Booking.BookingStatus.pending : Booking.BookingStatus.paid);
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
        payment.setAmount(total);
        payment.setOrderCode(booking.getBookingId());

        if (isPayOS) {
            payment.setMethod(Payment.PaymentMethod.online);
            payment.setPaymentStatus(Payment.PaymentStatus.pending);
        } else {
            payment.setMethod(Payment.PaymentMethod.cash);
            payment.setPaymentStatus(Payment.PaymentStatus.success);
            payment.setPaymentTime(java.time.LocalDateTime.now());
        }
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

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (booking.getStatus() == Booking.BookingStatus.paid && now.isAfter(booking.getShowtime().getStartTime())) {
            throw new IllegalArgumentException("Không thể hủy vé đã thanh toán sau khi suất chiếu bắt đầu.");
        }

        if (booking.getPromotion() != null) {
            Promotion promo = booking.getPromotion();

            if (promo.getUsedCount() != null && promo.getUsedCount() > 0) {
                promo.setUsedCount(promo.getUsedCount() - 1);
                promotionRepository.save(promo);
            }
        }

        booking.setStatus(Booking.BookingStatus.cancelled);
        bookingRepository.save(booking);

        paymentRepository.findByBooking(booking).ifPresent(payment -> {
            payment.setPaymentStatus(Payment.PaymentStatus.failed);
            paymentRepository.save(payment);
        });
    }
}