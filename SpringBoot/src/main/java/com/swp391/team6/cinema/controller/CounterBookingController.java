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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff/booking")
@RequiredArgsConstructor
public class CounterBookingController {

    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CounterBookingService counterBookingService;
    private final BookingService bookingService;
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

        Page<Movie> moviePage = counterBookingService.getMoviesForPOS(user, keyword, status, genre, PageRequest.of(page, 12));

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
        BigDecimal total = counterBookingService.calculateTotalPrice(showtimeId, seatIds);
        return ResponseEntity.ok(Map.of("totalAmount", total));
    }

    @GetMapping("/api/showtimes")
    @ResponseBody
    public List<ShowtimeDTO> getShowtimes(@RequestParam Long movieId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return showtimeRepository.findByMovieAndBranch(movieId, user.getBranchId()).stream().map(s -> {
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
        return counterBookingService.getSeatStatuses(showtimeId);
    }

    @PostMapping("/api/lock-seats")
    @ResponseBody
    public ResponseEntity<?> lockSeats(@RequestParam Long showtimeId, @RequestBody List<Long> seatIds) {
        for (Long seatId : seatIds) {
            if (!seatLockService.lockSeat(showtimeId, seatId)) return ResponseEntity.badRequest().body(Map.of("message", "Ghế đã có người giữ!"));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/unlock-seats")
    @ResponseBody
    public ResponseEntity<?> unlockSeats(@RequestParam Long showtimeId, @RequestBody List<Long> seatIds) {
        if (seatIds != null) seatIds.forEach(id -> seatLockService.releaseSeat(showtimeId, id));
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
        return userRepository.findByFullNameContainingIgnoreCaseAndRole(name, User.UserRole.CUSTOMER).stream().limit(10).map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("fullName", u.getFullName());
            m.put("phone", u.getPhone());
            m.put("email", u.getEmail());
            return m;
        }).collect(Collectors.toList());
    }

    @GetMapping("/api/validate-promo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validatePromo(@RequestParam String promoCode, @RequestParam BigDecimal currentTotal, HttpSession session) {
        User staff = (User) session.getAttribute("loggedInUser");
        try {
            return ResponseEntity.ok(counterBookingService.validatePromotion(promoCode, currentTotal, staff.getBranchId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/confirm-booking")
    @ResponseBody
    public ResponseEntity<?> confirmBooking(@RequestBody BookingRequest request, HttpSession session) {
        User staff = (User) session.getAttribute("loggedInUser");
        if (staff == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));

        try {
            Booking booking = counterBookingService.processCounterBooking(staff, request.getShowtimeId(), request.getSeatIds(), request.getPhone(), request.getEmail(), request.getPaymentMethod().toLowerCase(), request.getPromoCode());

            if ("payos".equalsIgnoreCase(request.getPaymentMethod())) {
                String checkoutUrl = payOSService.createPaymentLink(booking.getBookingId(), booking.getTotalAmount().intValue(), "Quầy - Đơn #" + booking.getBookingId(), "/staff/booking/success?bookingId=" + booking.getBookingId(), "/staff/booking/payment-cancel?bookingId=" + booking.getBookingId());
                if (checkoutUrl != null) return ResponseEntity.ok(Map.of("status", "PAYMENT_REQUIRED", "checkoutUrl", checkoutUrl, "bookingId", booking.getBookingId()));
            }
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Đặt vé thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam Long bookingId, Model model) {
        bookingService.syncPaymentStatusFromPayOS(bookingId);
        bookingService.getBookingDetail(bookingId).ifPresent(b -> model.addAttribute("booking", b));
        return "staff/booking-success-pos";
    }

    @GetMapping("/payment-cancel")
    public String counterPaymentCancel(@RequestParam(required = false) Long bookingId) {
        if (bookingId != null) { try { counterBookingService.cancelBooking(bookingId); } catch (Exception e) {} }
        return "staff/payment-cancel-pos";
    }

    @GetMapping("/manage")
    public String manageTickets(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String keyword, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.STAFF) return "redirect:/auth/login";

        Page<Booking> bookingPage = bookingRepository.findByBranchIdWithDetails(user.getBranchId(), (keyword == null) ? "" : keyword, PageRequest.of(page, 15));
        model.addAttribute("bookingPage", bookingPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        return "staff/manage-tickets";
    }

    @GetMapping("/api/booking-detail/{id}")
    @ResponseBody
    public ResponseEntity<?> getBookingDetail(@PathVariable Long id) {
        return ResponseEntity.ok(counterBookingService.getBookingDetailSummary(id));
    }

    @GetMapping("/print/{id}")
    public ResponseEntity<byte[]> printTicketToPdf(@PathVariable Long id) throws Exception {
        Booking booking = bookingRepository.findByIdWithDetails(id).orElseThrow();
        String seatNames = booking.getBookingSeats().stream().map(bs -> bs.getSeat().getSeatRow() + bs.getSeat().getSeatNumber()).sorted().collect(Collectors.joining(", "));

        Context context = new Context();
        context.setVariable("booking", booking);
        context.setVariable("seatNames", seatNames);

        String htmlContent = templateEngine.process("staff/print-ticket", context);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        try {
            // Nạp font từ thư mục resources
            String fontPath = "/fonts/DejaVuSans.ttf"; // Đường dẫn trong resources
            var fontUrl = getClass().getResource(fontPath);

            if (fontUrl != null) {
                // Identity-H là bắt buộc để hiển thị tiếng Việt
                renderer.getFontResolver().addFont(fontUrl.toString(),
                        com.lowagie.text.pdf.BaseFont.IDENTITY_H,
                        com.lowagie.text.pdf.BaseFont.EMBEDDED);
            } else {
                // Nếu không muốn dùng file trong resources, hãy dùng đường dẫn Windows nhưng phải chuẩn
                renderer.getFontResolver().addFont("C:/Windows/Fonts/tahoma.ttf",
                        com.lowagie.text.pdf.BaseFont.IDENTITY_H,
                        com.lowagie.text.pdf.BaseFont.EMBEDDED);
            }
        } catch (Exception e) {
            System.err.println("Lỗi nạp font: " + e.getMessage());
        }

        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Ticket_" + id + ".pdf\"");
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

    private CustomerDTO toCustomerDTO(User u) {
        return new CustomerDTO(u.getUserId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getStatus().toString(), u.getCreatedAt());
    }

    @Data
    public static class BookingRequest {
        private Long showtimeId;
        private List<Long> seatIds;
        private String phone, email, paymentMethod, promoCode;
    }
}