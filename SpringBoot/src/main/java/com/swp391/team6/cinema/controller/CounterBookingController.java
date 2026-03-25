package com.swp391.team6.cinema.controller;

import com.lowagie.text.pdf.BaseFont;
import com.swp391.team6.cinema.dto.CustomerDTO;
import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.BookingRepository;
import com.swp391.team6.cinema.repository.MovieRepository;
import com.swp391.team6.cinema.repository.UserRepository;
import com.swp391.team6.cinema.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
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

    private final CounterBookingService counterBookingService;
    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final MovieService movieService;
    private final BookingService bookingService;
    private final SeatLockService seatLockService;
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

        model.addAttribute("moviePage", (user.getBranchId() != null)
                ? movieRepository.findMoviesForPOS(user.getBranchId(), keyword, status, genre, PageRequest.of(page, 12))
                : movieRepository.findMoviesForPOSAllBranches(keyword, status, genre, PageRequest.of(page, 12)));

        model.addAttribute("genreList", movieService.getAllGenres());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("genre", genre);
        return "staff/pos-booking";
    }

    @GetMapping("/api/showtimes")
    @ResponseBody
    public List<?> getShowtimes(@RequestParam Long movieId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return counterBookingService.getShowtimesForMovie(movieId, user.getBranchId());
    }

    @GetMapping("/api/seats")
    @ResponseBody
    public List<Map<String, Object>> getSeats(@RequestParam Long showtimeId) {
        return counterBookingService.getSeatsWithStatus(showtimeId);
    }

    @PostMapping("/api/lock-seats")
    @ResponseBody
    public ResponseEntity<?> lockSeats(@RequestParam Long showtimeId, @RequestBody List<Long> seatIds) {
        try {
            for (Long id : seatIds) {
                if (!seatLockService.lockSeat(showtimeId, id)) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Ghế đã có người khác đang chọn!"));
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping("/api/unlock-seats")
    @ResponseBody
    public ResponseEntity<?> unlockSeats(@RequestParam Long showtimeId, @RequestBody List<Long> seatIds) {
        try {
            if (seatIds != null) {
                seatIds.forEach(id -> seatLockService.releaseSeat(showtimeId, id));
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/api/calculate-price")
    @ResponseBody
    public Map<String, Object> calculatePrice(@RequestParam Long showtimeId, @RequestParam List<Long> seatIds) {
        return Map.of("totalAmount", counterBookingService.calculateOriginalPrice(showtimeId, seatIds));
    }

    @GetMapping("/api/validate-promo")
    @ResponseBody
    public ResponseEntity<?> validatePromo(@RequestParam String promoCode, @RequestParam BigDecimal currentTotal, HttpSession session) {
        try {
            User staff = (User) session.getAttribute("loggedInUser");
            return ResponseEntity.ok(counterBookingService.validatePromotion(promoCode, currentTotal, staff.getBranchId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/find-user")
    @ResponseBody
    public ResponseEntity<CustomerDTO> findUser(@RequestParam String contact) {
        return counterBookingService.findCustomer(contact).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/search-users")
    @ResponseBody
    public List<Map<String, Object>> searchUsers(@RequestParam(required = false, defaultValue = "") String name) {
        return userRepository.findByFullNameContainingIgnoreCaseAndRole(name, User.UserRole.CUSTOMER)
                .stream()
                .limit(10)
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("fullName", u.getFullName());
                    map.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    map.put("email", u.getEmail() != null ? u.getEmail() : "");
                    return map;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/api/confirm-booking")
    @ResponseBody
    public ResponseEntity<?> confirmBooking(@RequestBody BookingRequestDTO request, HttpSession session) {
        User staff = (User) session.getAttribute("loggedInUser");
        if (staff == null) return ResponseEntity.status(401).build();

        try {
            Booking booking = counterBookingService.processCounterBooking(staff, request.getShowtimeId(), request.getSeatIds(),
                    request.getPhone(), request.getEmail(), request.getPaymentMethod(), request.getPromoCode());

            if ("payos".equalsIgnoreCase(request.getPaymentMethod())) {
                String checkoutUrl = payOSService.createPaymentLink(booking.getBookingId(), booking.getTotalAmount().intValue(),
                        "Quầy-#" + booking.getBookingId(), "/staff/booking/success?bookingId=" + booking.getBookingId(),
                        "/staff/booking/payment-cancel?bookingId=" + booking.getBookingId());
                return ResponseEntity.ok(Map.of("status", "PAYMENT_REQUIRED", "checkoutUrl", checkoutUrl, "bookingId", booking.getBookingId()));
            }
            return ResponseEntity.ok(Map.of("status", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/print/{id}")
    public ResponseEntity<byte[]> printTicket(@PathVariable Long id) throws Exception {
        Booking b = bookingRepository.findByIdWithDetails(id).orElseThrow();
        String seatNames = b.getBookingSeats().stream().map(bs -> bs.getSeat().getSeatRow() + bs.getSeat().getSeatNumber()).sorted().collect(Collectors.joining(", "));

        Context context = new Context();
        context.setVariable("booking", b);
        context.setVariable("seatNames", seatNames);

        String html = templateEngine.process("staff/print-ticket", context);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.getFontResolver().addFont("/Windows/Fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(out);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "Ticket_" + id + ".pdf");
        return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
    }

    @GetMapping("/manage")
    public String manageTickets(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String keyword, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.STAFF) return "redirect:/auth/login";

        model.addAttribute("bookingPage", bookingRepository.findByBranchIdWithDetails(user.getBranchId(), keyword == null ? "" : keyword, PageRequest.of(page, 15)));
        model.addAttribute("keyword", keyword);
        return "staff/manage-tickets";
    }

    @PostMapping("/api/cancel/{id}")
    @ResponseBody
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        try {
            counterBookingService.cancelBooking(id);
            return ResponseEntity.ok(Map.of("message", "Thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Data
    public static class BookingRequestDTO {
        private Long showtimeId;
        private List<Long> seatIds;
        private String phone;
        private String email;
        private String paymentMethod;
        private String promoCode;
    }
}