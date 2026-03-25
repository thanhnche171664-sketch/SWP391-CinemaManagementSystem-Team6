package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final MovieRepository movieRepository;

    /**
     * Lấy danh sách phim cho POS dựa trên chi nhánh của Staff
     */
    public Page<Movie> getMoviesForPOS(User staff, String keyword, Movie.MovieStatus status, String genre, Pageable pageable) {
        String normalizedKeyword = (keyword != null && !keyword.isEmpty()) ? keyword : null;
        String normalizedGenre = (genre != null && !genre.isEmpty()) ? genre : null;
        Long branchId = staff.getBranchId();

        if (branchId != null) {
            Page<Movie> moviePage = movieRepository.findMoviesForPOS(branchId, normalizedKeyword, status, normalizedGenre, pageable);
            if (!moviePage.isEmpty()) return moviePage;
        }
        return movieRepository.findMoviesForPOSAllBranches(normalizedKeyword, status, normalizedGenre, pageable);
    }

    /**
     * Tính toán tổng tiền dựa trên danh sách ghế và suất chiếu
     */
    public BigDecimal calculateTotalPrice(Long showtimeId, List<Long> seatIds) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại"));
        List<Seat> seats = seatRepository.findAllById(seatIds);
        Long branchId = showtime.getRoom().getBranch().getBranchId();

        return seats.stream()
                .map(s -> pricingService.getPrice(branchId, s.getSeatType(), showtime.getStartTime()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Lấy trạng thái ghế (Trống/Đã đặt/Đang khóa)
     */
    public List<Map<String, Object>> getSeatStatuses(Long showtimeId) {
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

    /**
     * Kiểm tra mã giảm giá
     */
    public Map<String, Object> validatePromotion(String promoCode, BigDecimal currentTotal, Long branchId) {
        Promotion promo = promotionRepository.findByPromoCode(promoCode)
                .orElseThrow(() -> new IllegalArgumentException("Mã không tồn tại"));

        if (promo.getStatus() != Promotion.Status.active || promo.getBranch() == null) {
            throw new IllegalArgumentException("Mã giảm giá không khả dụng");
        }
        if (!promo.getBranch().getBranchId().equals(branchId)) {
            throw new IllegalArgumentException("Mã không áp dụng cho chi nhánh này");
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (promo.getEndDate() != null && now.isAfter(promo.getEndDate())) {
            throw new IllegalArgumentException("Mã đã hết hạn");
        }
        if (currentTotal.compareTo(promo.getMinBookingAmount()) < 0) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu");
        }

        BigDecimal discount = (promo.getDiscountType() == Promotion.DiscountType.percent)
                ? currentTotal.multiply(promo.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : promo.getDiscountValue();

        Map<String, Object> response = new HashMap<>();
        response.put("discountAmount", discount);
        response.put("finalAmount", currentTotal.subtract(discount).max(BigDecimal.ZERO));
        return response;
    }

    /**
     * Xử lý đặt vé tại quầy (Logic nguyên bản từ Controller cũ)
     */
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
            // Validation logic (giữ nguyên logic gốc)
            if (appliedPromo == null || appliedPromo.getStatus() != Promotion.Status.active) throw new IllegalArgumentException("Mã không khả dụng");
            if (!appliedPromo.getBranch().getBranchId().equals(branchId)) throw new IllegalArgumentException("Mã không thuộc chi nhánh");

            BigDecimal discount = (appliedPromo.getDiscountType() == Promotion.DiscountType.percent)
                    ? total.multiply(appliedPromo.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                    : appliedPromo.getDiscountValue();

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
        if (appliedPromo != null) booking.setPromotion(appliedPromo);

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

        for (Long sid : seatIds) seatLockService.releaseSeat(showtimeId, sid);

        return booking;
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé"));

        if (booking.getStatus() == Booking.BookingStatus.cancelled) throw new IllegalArgumentException("Vé đã hủy");

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (booking.getStatus() == Booking.BookingStatus.paid && now.isAfter(booking.getShowtime().getStartTime())) {
            throw new IllegalArgumentException("Không thể hủy sau khi suất chiếu bắt đầu");
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

        paymentRepository.findByBooking(booking).ifPresent(p -> {
            p.setPaymentStatus(Payment.PaymentStatus.failed);
            paymentRepository.save(p);
        });
    }

    public Map<String, Object> getBookingDetailSummary(Long id) {
        Booking b = bookingRepository.findByIdWithDetails(id).orElseThrow();
        Map<String, Object> map = new HashMap<>();
        map.put("bookingId", b.getBookingId());
        map.put("customer", b.getUser().getFullName());
        map.put("email", b.getUser().getEmail() != null ? b.getUser().getEmail() : "N/A");
        map.put("phone", b.getUser().getPhone() != null ? b.getUser().getPhone() : "N/A");
        map.put("movie", b.getShowtime().getMovie().getTitle());
        map.put("room", b.getShowtime().getRoom().getRoomName());
        map.put("startTime", b.getShowtime().getStartTime().toString());
        map.put("seats", b.getBookingSeats().stream().map(bs -> bs.getSeat().getSeatRow() + bs.getSeat().getSeatNumber()).collect(Collectors.joining(", ")));
        map.put("total", b.getTotalAmount());
        map.put("promo", b.getPromotion() != null ? b.getPromotion().getPromoCode() : "Không sử dụng");
        return map;
    }
}