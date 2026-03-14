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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff/booking")
@RequiredArgsConstructor
public class StaffBookingController {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final StaffBookingService staffBookingService;
    private final BookingService bookingService;
    private final PricingService pricingService;
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
        Page<Movie> moviePage = movieRepository.findMoviesForPOS(user.getBranchId(), (keyword != null && !keyword.isEmpty()) ? keyword : null, status, (genre != null && !genre.isEmpty()) ? genre : null, pageable);

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
            map.put("isBooked", occupied.contains(s.getSeatId()));
            return map;
        }).collect(Collectors.toList());
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

    @PostMapping("/api/confirm-booking")
    @ResponseBody
    public ResponseEntity<?> confirmBooking(@RequestBody BookingRequest request, HttpSession session) {
        User staff = (User) session.getAttribute("loggedInUser");
        if (staff == null) return ResponseEntity.status(401).body("Chưa đăng nhập");
        try {
            Booking booking = staffBookingService.processCounterBooking(staff, request.getShowtimeId(), request.getSeatIds(), request.getPhone(), request.getEmail(), request.getPaymentMethod());
            if ("ONLINE".equals(request.getPaymentMethod())) {
                String paymentUrl = payOSService.createPaymentLink(booking.getBookingId(), booking.getTotalAmount().intValue(), "Thanh toán #" + booking.getBookingId(), "/booking/success", "/booking/cancel");
                if (paymentUrl != null) {
                    bookingService.setPaymentLinkId(booking.getBookingId(), paymentUrl);
                    return ResponseEntity.ok(Map.of("status", "REDIRECT", "url", paymentUrl));
                }
                throw new Exception("Không thể tạo link thanh toán");
            }
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Đặt vé thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    private CustomerDTO toCustomerDTO(User u) {
        return new CustomerDTO(u.getUserId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getStatus().toString(), u.getCreatedAt());
    }

    @GetMapping("/manage")
    public String manageTickets(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.STAFF) return "redirect:/auth/login";
        List<Booking> bookings = bookingRepository.findByBranchIdWithDetails(user.getBranchId());
        model.addAttribute("bookings", bookings);
        return "staff/manage-tickets";
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

    @Data
    public static class BookingRequest {
        private Long showtimeId;
        private List<Long> seatIds;
        private String phone;
        private String email;
        private String paymentMethod;
    }
}