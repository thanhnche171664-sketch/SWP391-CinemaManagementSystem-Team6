package com.swp391.team6.cinema.controller;

import com.lowagie.text.pdf.BaseFont;
import com.swp391.team6.cinema.dto.CustomerDTO;
import com.swp391.team6.cinema.dto.ShowtimeDTO;
import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.*;
import com.swp391.team6.cinema.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff/booking")
@RequiredArgsConstructor
public class CounterBookingController {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final PromotionRepository promotionRepository;
    private final CounterBookingService counterBookingService;
    private final BookingService bookingService;
    private final PricingService pricingService;
    private final SeatLockService seatLockService;
    private final MovieService movieService;
    private final PayOSService payOSService;
    private final TemplateEngine templateEngine;



    @GetMapping("/pos")
    public String posView(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Movie.MovieStatus status,
                          @RequestParam(required = false) String genre,
                          HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.STAFF) return "redirect:/auth/login";

        Pageable pageable = PageRequest.of(page, 12);
        String normalizedKeyword = (keyword != null && !keyword.isEmpty()) ? keyword : null;
        String normalizedGenre = (genre != null && !genre.isEmpty()) ? genre : null;
        Long branchId = user.getBranchId();

        Page<Movie> moviePage;
        if (branchId != null) {
            moviePage = movieRepository.findMoviesForPOS(branchId, normalizedKeyword, status, normalizedGenre, pageable);
            if (moviePage.isEmpty()) {
                // Fallback to avoid blank POS when branch mapping data is missing.
                moviePage = movieRepository.findMoviesForPOSAllBranches(normalizedKeyword, status, normalizedGenre, pageable);
            }
        } else {
            moviePage = movieRepository.findMoviesForPOSAllBranches(normalizedKeyword, status, normalizedGenre, pageable);
        }

        model.addAttribute("moviePage", moviePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("genre", genre);
        model.addAttribute("genreList", movieService.getAllGenres());
        model.addAttribute("movieStatuses", Movie.MovieStatus.values());
        return "staff/pos-booking";
    }

    @GetMapping("/api/calculate-price")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> calculatePrice(@RequestParam Long showtimeId, @RequestParam List<Long> seatIds) {
        Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow();
        List<Seat> seats = seatRepository.findAllById(seatIds);
        Long branchId = showtime.getRoom().getBranch().getBranchId();

        BigDecimal total = seats.stream()
                .map(s -> pricingService.getPrice(branchId, s.getSeatType(), showtime.getStartTime()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        response.put("totalAmount", total);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/showtimes")
    @ResponseBody
    public List<ShowtimeDTO> getShowtimes(@RequestParam Long movieId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        List<Showtime> showtimes = showtimeRepository.findByMovieAndBranch(movieId, user.getBranchId());
        return showtimes.stream().map(s -> {
            ShowtimeDTO dto = new ShowtimeDTO();
            dto.setShowtimeId(s.getShowtimeId());
            dto.setStartTime(s.getStartTime());
            dto.setRoomName(s.getRoom().getRoomName());
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/api/seats")
    @ResponseBody
    public List<Map<String, Object>> getSeats(@RequestParam Long showtimeId) {
        Showtime st = showtimeRepository.findById(showtimeId).orElseThrow();
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

    @PostMapping("/api/lock-seats")
    @ResponseBody
    public ResponseEntity<?> lockSeats(@RequestParam Long showtimeId, @RequestBody List<Long> seatIds) {
        for (Long seatId : seatIds) {
            if (!seatLockService.lockSeat(showtimeId, seatId)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Ghế đã có người giữ!"));
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/unlock-seats")
    @ResponseBody
    public ResponseEntity<?> unlockSeats(@RequestParam Long showtimeId, @RequestBody List<Long> seatIds) {
        if (seatIds != null) {
            seatIds.forEach(id -> seatLockService.releaseSeat(showtimeId, id));
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/find-user")
    @ResponseBody
    public ResponseEntity<CustomerDTO> findUser(@RequestParam String phone) {
        return userRepository.findByPhone(phone).map(u -> ResponseEntity.ok(toCustomerDTO(u))).orElse(ResponseEntity.status(404).build());
    }

    @GetMapping("/api/find-user-by-email")
    @ResponseBody
    public ResponseEntity<CustomerDTO> findUserByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email).map(u -> ResponseEntity.ok(toCustomerDTO(u))).orElse(ResponseEntity.status(404).build());
    }

    @GetMapping("/api/search-users")
    @ResponseBody
    public List<Map<String, Object>> searchUsers(@RequestParam(required = false, defaultValue = "") String name) {
        List<User> users = userRepository.findByFullNameContainingIgnoreCaseAndRole(name, User.UserRole.CUSTOMER);

        return users.stream().limit(10).map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("fullName", u.getFullName());
            m.put("phone", u.getPhone());
            m.put("email", u.getEmail());
            return m;
        }).collect(Collectors.toList());
    }

    @GetMapping("/api/validate-promo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validatePromo(
            @RequestParam String promoCode,
            @RequestParam BigDecimal currentTotal,
            HttpSession session) {

        User staff = (User) session.getAttribute("loggedInUser");
        Long branchId = staff.getBranchId();

        Promotion promo = promotionRepository.findByPromoCode(promoCode)
                .orElse(null);
        if (promo == null || promo.getStatus() != Promotion.Status.active) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã không hợp lệ"));
        }
        if (promo.getBranch() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã giảm giá hiện không hoạt động"));
        }

        if (!promo.getBranch().getBranchId().equals(branchId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã không áp dụng cho chi nhánh này"));
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (promo.getEndDate() != null && now.isAfter(promo.getEndDate())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã đã hết hạn"));
        }

        if (currentTotal.compareTo(promo.getMinBookingAmount()) < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Đơn hàng chưa đạt giá trị tối thiểu"));
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (promo.getDiscountType() == Promotion.DiscountType.percent) {
            discount = currentTotal.multiply(promo.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            discount = promo.getDiscountValue();
        }

        BigDecimal finalAmount = currentTotal.subtract(discount).max(BigDecimal.ZERO);

        Map<String, Object> response = new HashMap<>();
        response.put("discountAmount", discount);
        response.put("finalAmount", finalAmount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/confirm-booking")
    @ResponseBody
    public ResponseEntity<?> confirmBooking(@RequestBody BookingRequest request, HttpSession session) {
        User staff = (User) session.getAttribute("loggedInUser");
        if (staff == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));

        try {
            Booking booking = counterBookingService.processCounterBooking(
                    staff,
                    request.getShowtimeId(),
                    request.getSeatIds(),
                    request.getPhone(),
                    request.getEmail(),
                    request.getPaymentMethod().toLowerCase(),
                    request.getPromoCode()
            );

            if ("payos".equalsIgnoreCase(request.getPaymentMethod())) {
                int amountVnd = booking.getTotalAmount().intValue();
                String desc = "Quầy - Đơn #" + booking.getBookingId();

                String returnPath = "/staff/booking/success?bookingId=" + booking.getBookingId();
                String cancelPath = "/staff/booking/payment-cancel?bookingId=" + booking.getBookingId();

                String checkoutUrl = payOSService.createPaymentLink(
                        booking.getBookingId(), amountVnd, desc, returnPath, cancelPath
                );

                if (checkoutUrl != null) {
                    return ResponseEntity.ok(Map.of(
                            "status", "PAYMENT_REQUIRED",
                            "checkoutUrl", checkoutUrl,
                            "bookingId", booking.getBookingId()
                    ));
                }
            }

            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Đặt vé thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam Long bookingId, Model model) {
        bookingService.syncPaymentStatusFromPayOS(bookingId);

        var detail = bookingService.getBookingDetail(bookingId);
        if (detail.isPresent()) {
            model.addAttribute("booking", detail.get());
            return "staff/booking-success-pos";
        }
        return "redirect:/staff/booking/pos";
    }

    @GetMapping("/payment-cancel")
    public String counterPaymentCancel(@RequestParam(required = false) Long bookingId, Model model) {
        if (bookingId != null) {
            try {
                counterBookingService.cancelBooking(bookingId);
            } catch (Exception e) {
            }
        }
        return "staff/payment-cancel-pos";
    }

    private CustomerDTO toCustomerDTO(User u) {
        return new CustomerDTO(u.getUserId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getStatus().toString(), u.getCreatedAt());
    }

    @GetMapping("/manage")
    public String manageTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.STAFF) return "redirect:/auth/login";

        Pageable pageable = PageRequest.of(page, 15);
        String searchName = (keyword == null || keyword.trim().isEmpty()) ? "" : keyword;
        Page<Booking> bookingPage = bookingRepository.findByBranchIdWithDetails(
                user.getBranchId(), searchName, pageable);

        model.addAttribute("bookingPage", bookingPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        return "staff/manage-tickets";
    }

    @GetMapping("/api/booking-detail/{id}")
    @ResponseBody
    public ResponseEntity<?> getBookingDetail(@PathVariable Long id) {
        Booking b = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé"));

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

        return ResponseEntity.ok(map);
    }

    @GetMapping("/print/{id}")
    public ResponseEntity<byte[]> printTicketToPdf(@PathVariable Long id) throws Exception {
        Booking booking = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé"));

        String seatNames = booking.getBookingSeats().stream()
                .map(bs -> bs.getSeat().getSeatRow() + bs.getSeat().getSeatNumber())
                .sorted()
                .collect(Collectors.joining(", "));

        Context context = new Context();
        context.setVariable("booking", booking);
        context.setVariable("seatNames", seatNames);

        String htmlContent = templateEngine.process("staff/print-ticket", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        String fontPath = "/Windows/Fonts/arial.ttf";
        renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "Ticket_" + id + ".pdf");
        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }

    @PostMapping("/api/cancel/{id}")
    @ResponseBody
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            counterBookingService.cancelBooking(id);
            return ResponseEntity.ok(Map.of("message", "Hủy vé thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Data
    public static class BookingRequest {
        private Long showtimeId;
        private List<Long> seatIds;
        private String phone;
        private String email;
        private String paymentMethod;
        private String promoCode;
    }
}