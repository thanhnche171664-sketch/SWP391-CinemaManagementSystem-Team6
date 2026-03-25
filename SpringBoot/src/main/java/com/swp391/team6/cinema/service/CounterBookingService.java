package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.CustomerDTO;
import com.swp391.team6.cinema.dto.ShowtimeDTO;
import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<ShowtimeDTO> getShowtimesForMovie(Long movieId, Long branchId) {
        List<Showtime> showtimes = showtimeRepository.findByMovieAndBranch(movieId, branchId);
        return showtimes.stream().map(s -> {
            ShowtimeDTO dto = new ShowtimeDTO();
            dto.setShowtimeId(s.getShowtimeId());
            dto.setStartTime(s.getStartTime());
            dto.setRoomName(s.getRoom().getRoomName());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getSeatsWithStatus(Long showtimeId) {
        Showtime st = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại"));
        List<Seat> allSeats = seatRepository.findByRoomRoomIdOrderBySeatRowAscSeatNumberAsc(st.getRoom().getRoomId());
        Set<Long> occupied = bookingService.getOccupiedSeatIdsForShowtime(showtimeId);

        return allSeats.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("seatId", s.getSeatId());
            map.put("seatRow", s.getSeatRow());
            map.put("seatNumber", s.getSeatNumber());
            map.put("seatType", s.getSeatType() != null ? s.getSeatType().toString() : "NORMAL");
            boolean isLocked = seatLockService.isLocked(showtimeId, s.getSeatId());
            map.put("isBooked", occupied.contains(s.getSeatId()) || isLocked);
            return map;
        }).collect(Collectors.toList());
    }

    public BigDecimal calculateOriginalPrice(Long showtimeId, List<Long> seatIds) {
        Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow();
        List<Seat> seats = seatRepository.findAllById(seatIds);
        Long branchId = showtime.getRoom().getBranch().getBranchId();

        return seats.stream()
                .map(s -> pricingService.getPrice(branchId, s.getSeatType(), showtime.getStartTime()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, Object> validatePromotion(String promoCode, BigDecimal currentTotal, Long branchId) {
        Promotion promo = promotionRepository.findByPromoCode(promoCode)
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại"));

        if (promo.getStatus() != Promotion.Status.active || promo.getBranch() == null)
            throw new IllegalArgumentException("Mã giảm giá hiện không hoạt động");

        if (!promo.getBranch().getBranchId().equals(branchId))
            throw new IllegalArgumentException("Mã không áp dụng cho chi nhánh này");

        LocalDateTime now = LocalDateTime.now();
        if (promo.getEndDate() != null && now.isAfter(promo.getEndDate()))
            throw new IllegalArgumentException("Mã đã hết hạn");

        if (currentTotal.compareTo(promo.getMinBookingAmount()) < 0)
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu " + promo.getMinBookingAmount());

        BigDecimal discount = (promo.getDiscountType() == Promotion.DiscountType.percent)
                ? currentTotal.multiply(promo.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : promo.getDiscountValue();

        Map<String, Object> response = new HashMap<>();
        response.put("discountAmount", discount);
        response.put("finalAmount", currentTotal.subtract(discount).max(BigDecimal.ZERO));
        return response;
    }

    public Optional<CustomerDTO> findCustomer(String contact) {
        Optional<User> user = contact.contains("@")
                ? userRepository.findByEmail(contact)
                : userRepository.findByPhone(contact);

        return user.map(u -> new CustomerDTO(u.getUserId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getStatus().toString(), u.getCreatedAt()));
    }

    @Transactional
    public Booking processCounterBooking(User staff, Long showtimeId, List<Long> seatIds,
                                         String phone, String email, String paymentMethodStr,
                                         String promoCode) {

        boolean isPayOS = "payos".equalsIgnoreCase(paymentMethodStr) || "online".equalsIgnoreCase(paymentMethodStr);
        boolean isCash = "cash".equalsIgnoreCase(paymentMethodStr);

        if (!isPayOS && !isCash) throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ");

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại"));

        User customer = findCustomer(email != null && !email.isBlank() ? email : phone)
                .flatMap(dto -> userRepository.findById(dto.getUser_id()))
                .orElseGet(() -> userRepository.findFirstByRole(User.UserRole.GUEST)
                        .orElseThrow(() -> new IllegalArgumentException("Hệ thống chưa có tài khoản GUEST")));

        BigDecimal total = calculateOriginalPrice(showtimeId, seatIds);
        Promotion appliedPromo = null;

        if (promoCode != null && !promoCode.isBlank()) {
            Map<String, Object> promoResult = validatePromotion(promoCode, total, staff.getBranchId());
            appliedPromo = promotionRepository.findByPromoCode(promoCode).get();
            total = (BigDecimal) promoResult.get("finalAmount");

            appliedPromo.setUsedCount(appliedPromo.getUsedCount() + 1);
            promotionRepository.save(appliedPromo);
        }

        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setUser(customer);
        booking.setBookingType(Booking.BookingType.counter);
        booking.setStatus(isPayOS ? Booking.BookingStatus.pending : Booking.BookingStatus.paid);
        booking.setTotalAmount(total);
        if (appliedPromo != null) booking.setPromotion(appliedPromo);
        booking = bookingRepository.save(booking);

        for (Long sid : seatIds) {
            Seat seat = seatRepository.findById(sid).orElseThrow();
            BookingSeat bs = new BookingSeat();
            bs.setBooking(booking);
            bs.setSeat(seat);
            bookingSeatRepository.save(bs);
            seatLockService.releaseSeat(showtimeId, sid);
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(total);
        payment.setOrderCode(booking.getBookingId());
        payment.setMethod(isPayOS ? Payment.PaymentMethod.online : Payment.PaymentMethod.cash);
        payment.setPaymentStatus(isPayOS ? Payment.PaymentStatus.pending : Payment.PaymentStatus.success);
        if (isCash) payment.setPaymentTime(LocalDateTime.now());
        paymentRepository.save(payment);

        return booking;
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé"));

        if (booking.getStatus() == Booking.BookingStatus.cancelled) return;

        if (booking.getStatus() == Booking.BookingStatus.paid && LocalDateTime.now().isAfter(booking.getShowtime().getStartTime())) {
            throw new IllegalArgumentException("Không thể hủy vé sau khi suất chiếu bắt đầu");
        }

        if (booking.getPromotion() != null) {
            Promotion promo = booking.getPromotion();
            promo.setUsedCount(Math.max(0, promo.getUsedCount() - 1));
            promotionRepository.save(promo);
        }

        booking.setStatus(Booking.BookingStatus.cancelled);
        bookingRepository.save(booking);

        paymentRepository.findByBooking(booking).ifPresent(p -> {
            p.setPaymentStatus(Payment.PaymentStatus.failed);
            paymentRepository.save(p);
        });
    }
}