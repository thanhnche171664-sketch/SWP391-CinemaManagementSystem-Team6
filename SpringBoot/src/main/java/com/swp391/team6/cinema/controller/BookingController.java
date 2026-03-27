package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.entity.Showtime;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.SeatRepository;
import com.swp391.team6.cinema.repository.ShowtimeRepository;
import com.swp391.team6.cinema.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.naming.Context;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingService bookingService;
    private final PricingService pricingService;
    private final PayOSService payOSService;
    private final SeatService seatService;

    @GetMapping("/{showtimeId}")
    public String seatSelection(@org.springframework.web.bind.annotation.PathVariable Long showtimeId,
                                HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        return processSeatSelection(showtimeId, session, model, redirectAttributes);
    }

    @GetMapping("/seats")
    public String seatSelectionByParam(@RequestParam Long showtimeId,
                                       HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        return processSeatSelection(showtimeId, session, model, redirectAttributes);
    }

    private String processSeatSelection(Long showtimeId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đặt vé.");
            return "redirect:/auth/login?redirect=/booking/" + showtimeId;
        }
        bookingService.cancelExpiredPendingBookings();
        Showtime showtime = showtimeRepository.findByIdWithMovieRoomBranch(showtimeId).orElse(null);
        if (showtime == null) {
            redirectAttributes.addFlashAttribute("error", "Suất chiếu không tồn tại.");
            return "redirect:/movies";
        }
        List<Seat> seats = seatRepository.findByRoomRoomIdOrderBySeatRowAscSeatNumberAsc(showtime.getRoom().getRoomId());
        if (seats.isEmpty()) {
            int total = showtime.getRoom().getTotalSeats() != null ? showtime.getRoom().getTotalSeats() : 100;
            seats = seatService.getSeatsByRoomOrGenerate(showtime.getRoom().getRoomId(), total);
        }
        Set<Long> occupied = bookingService.getOccupiedSeatIdsForShowtime(showtimeId);
        Long branchId = showtime.getRoom().getBranch().getBranchId();
        Map<Long, BigDecimal> seatPrices = new HashMap<>();
        for (Seat s : seats) {
            seatPrices.put(s.getSeatId(), pricingService.getPrice(branchId, s.getSeatType(), showtime.getStartTime()));
        }
        Map<String, List<Seat>> seatsByRow = new LinkedHashMap<>();
        Set<String> seen = new HashSet<>();
        List<Seat> uniqueSeatsList = new ArrayList<>();
        
        for (Seat s : seats) {
            String key = s.getSeatRow() + ":" + s.getSeatNumber();
            if (!seen.contains(key)) {
                seen.add(key);
                uniqueSeatsList.add(s);
                seatsByRow.computeIfAbsent(s.getSeatRow(), k -> new ArrayList<>()).add(s);
            }
        }
        
        List<String> rows = new ArrayList<>(seatsByRow.keySet());
        rows.sort(Comparator.naturalOrder());

        // Create simplified data for JS to avoid infinite recursion
        List<Map<String, Object>> simplifiedSeats = uniqueSeatsList.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("seatId", s.getSeatId());
            map.put("seatRow", s.getSeatRow());
            map.put("seatNumber", s.getSeatNumber());
            map.put("seatType", s.getSeatType().toString());
            return map;
        }).toList();

        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", simplifiedSeats); // For JS
        model.addAttribute("rows", rows);
        model.addAttribute("seatsByRow", seatsByRow); // For Thymeleaf
        model.addAttribute("occupiedIds", occupied);
        model.addAttribute("seatPrices", seatPrices);
        model.addAttribute("user", user);


        return "booking-seat";
    }

    @PostMapping("/create")
    public String createBooking(@RequestParam Long showtimeId,
                                @RequestParam List<Long> seatIds,
                                @RequestParam(required = false) String promoCode,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        }
        try {
            var booking = bookingService.createBooking(user.getUserId(), showtimeId, seatIds, promoCode);
            int amountVnd = booking.getTotalAmount().intValue();
            if (amountVnd < 1000) amountVnd = 1000;
            String desc = "Booking #" + booking.getBookingId();
            String returnPath = "/booking/success?bookingId=" + booking.getBookingId();
            String cancelPath = "/booking/failed?bookingId=" + booking.getBookingId();
            String checkoutUrl = payOSService.createPaymentLink(booking.getBookingId(), amountVnd, desc, returnPath, cancelPath);
            if (checkoutUrl != null && !checkoutUrl.isBlank()) {
                return "redirect:" + checkoutUrl;
            }
            redirectAttributes.addFlashAttribute("error",
                    "Không thể tạo link thanh toán PayOS. Kiểm tra lại cấu hình hoặc liên hệ hỗ trợ.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi tạo thanh toán: " + e.getMessage());
        }
        return "redirect:/booking/" + showtimeId;
    }

    @GetMapping("/api/validate-promo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validatePromo(@RequestParam Long showtimeId,
                                                              @RequestParam String promoCode,
                                                              @RequestParam BigDecimal currentTotal) {
        try {
            return ResponseEntity.ok(bookingService.validatePromotion(promoCode, currentTotal, showtimeId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("valid", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/success")
    public String success(@RequestParam Long bookingId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        }
        var detail = bookingService.getBookingDetail(bookingId);
        if (detail.isEmpty() || !detail.get().getCustomerEmail().equals(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Đặt vé không tồn tại.");
            return "redirect:/booking/history";
        }
        if (detail.get().getStatus() == Booking.BookingStatus.pending) {
            bookingService.syncPaymentStatusFromPayOS(bookingId);
            detail = bookingService.getBookingDetail(bookingId);
        }
        detail.ifPresent(b -> model.addAttribute("booking", b));
        model.addAttribute("user", user);
        return "booking-success";
    }

    @GetMapping("/failed")
    public String failed(@RequestParam Long bookingId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/auth/login";
        }
        var detail = bookingService.getBookingDetail(bookingId);
        if (detail.isPresent() && detail.get().getCustomerEmail().equals(user.getEmail())) {
            if (detail.get().getStatus() == Booking.BookingStatus.pending) {
                bookingService.cancelPendingBooking(bookingId, user.getUserId());
                detail = bookingService.getBookingDetail(bookingId);
            }
            detail.ifPresent(b -> model.addAttribute("booking", b));
        }
        model.addAttribute("user", user);
        return "booking-failed";
    }

    @GetMapping("/history")
    public String history(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        }
        var list = bookingService.getBookingsByUserId(user.getUserId());
        model.addAttribute("bookings", list);
        model.addAttribute("user", user);
        return "booking-history";
    }

    @GetMapping("/detail/{bookingId}")
    public String detail(@org.springframework.web.bind.annotation.PathVariable Long bookingId,
                         HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        }
        var detail = bookingService.getBookingDetail(bookingId);
        if (detail.isEmpty() || !detail.get().getCustomerEmail().equals(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Đặt vé không tồn tại.");
            return "redirect:/booking/history";
        }
        model.addAttribute("booking", detail.get());
        model.addAttribute("user", user);
        return "booking-detail-customer";
    }

    // API endpoints for seat validation
    @GetMapping("/api/occupied-seats")
    @ResponseBody
    public Set<Long> getOccupiedSeats(@RequestParam Long showtimeId) {
        bookingService.cancelExpiredPendingBookings();
        return bookingService.getOccupiedSeatIdsForShowtime(showtimeId);
    }

    /**
     * Returns full seat status for a showtime — used by frontend polling.
     * {
     *   "paid":    [seatId, ...],   // paid bookings — lock vĩnh viễn
     *   "pending": [seatId, ...],   // pending bookings — đang chờ thanh toán
     *   "free":    [seatId, ...]    // available seats
     * }
     */
    @GetMapping("/api/seats/status")
    @ResponseBody
    public Map<String, Object> getSeatsStatus(@RequestParam Long showtimeId) {
        bookingService.cancelExpiredPendingBookings();
        return bookingService.getSeatsStatusForShowtime(showtimeId);
    }

    @PostMapping("/api/validate-seats")
    @ResponseBody
    public Map<String, Object> validateSeats(@RequestBody Map<String, Object> request) {
        Long showtimeId = Long.valueOf(request.get("showtimeId").toString());
        @SuppressWarnings("unchecked")
        List<Long> seatIds = (List<Long>) request.get("seatIds");
        Set<Long> occupied = bookingService.getOccupiedSeatIdsForShowtime(showtimeId);

        List<Long> occupiedInRequest = seatIds.stream()
                .filter(occupied::contains)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("valid", occupiedInRequest.isEmpty());
        result.put("occupiedSeats", occupiedInRequest);
        return result;
    }

    /** Debug endpoint — test PayOS credentials and connectivity */
    @GetMapping("/api/payos-test")
    @ResponseBody
    public Map<String, Object> payosTest() {
        Map<String, Object> result = new HashMap<>();
        result.put("clientIdConfigured", payOSService.isConfigured());
        result.put("canReachApi", payOSService.canReachApi());
        return result;
    }
}
