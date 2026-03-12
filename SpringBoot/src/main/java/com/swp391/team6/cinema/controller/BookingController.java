package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.entity.Showtime;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.SeatRepository;
import com.swp391.team6.cinema.repository.ShowtimeRepository;
import com.swp391.team6.cinema.service.BookingService;
import com.swp391.team6.cinema.service.PayOSService;
import com.swp391.team6.cinema.service.PricingService;
import com.swp391.team6.cinema.service.SeatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
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
        List<String> rows = seats.stream().map(Seat::getSeatRow).distinct().sorted().toList();
        Map<String, List<Seat>> seatsByRow = seats.stream().collect(Collectors.groupingBy(Seat::getSeatRow));
        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seats);
        model.addAttribute("rows", rows);
        model.addAttribute("seatsByRow", seatsByRow);
        model.addAttribute("occupiedIds", occupied);
        model.addAttribute("seatPrices", seatPrices);
        model.addAttribute("user", user);
        return "booking-seat";
    }

    @PostMapping("/create")
    public String createBooking(@RequestParam Long showtimeId,
                               @RequestParam List<Long> seatIds,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        }
        try {
            var booking = bookingService.createBooking(user.getUserId(), showtimeId, seatIds);
            int amountVnd = booking.getTotalAmount().intValue();
            if (amountVnd < 1000) amountVnd = 1000;
            String desc = "Booking #" + booking.getBookingId();
            String returnPath = "/booking/success?bookingId=" + booking.getBookingId();
            String cancelPath = "/booking/failed?bookingId=" + booking.getBookingId();
            String checkoutUrl = payOSService.createPaymentLink(booking.getBookingId(), amountVnd, desc, returnPath, cancelPath);
            if (checkoutUrl != null) {
                return "redirect:" + checkoutUrl;
            }
            redirectAttributes.addFlashAttribute("error", "Không thể tạo link thanh toán. Vui lòng thử lại.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/booking/" + showtimeId;
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
}
