package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.entity.Showtime;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.security.CustomUserDetails;
import com.swp391.team6.cinema.service.BookingService;
import com.swp391.team6.cinema.service.PayOSService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    private final PayOSService payOSService;

    public BookingController(BookingService bookingService, PayOSService payOSService) {
        this.bookingService = bookingService;
        this.payOSService = payOSService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUser();
        }
        return null;
    }

    @GetMapping("/select-seats")
    public String selectSeats(@RequestParam Long showtimeId, HttpServletRequest request, Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login?redirect=/booking/select-seats?showtimeId=" + showtimeId;
        }
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrf != null) model.addAttribute("_csrf", csrf);
        Showtime showtime = bookingService.getShowtimeById(showtimeId);
        List<Seat> seats = bookingService.getSeatsWithAvailability(showtimeId);
        Set<Long> occupied = bookingService.getOccupiedSeatIds(showtimeId);
        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seats);
        model.addAttribute("occupiedIds", occupied);
        return "select-seats";
    }

    @PostMapping("/create")
    public String createBooking(@RequestParam Long showtimeId, @RequestParam List<Long> seatIds, Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        try {
            Booking booking = bookingService.createBooking(showtimeId, seatIds, user);
            return "redirect:/booking/payment?bookingId=" + booking.getBookingId();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/booking/select-seats?showtimeId=" + showtimeId + "&error=" + e.getMessage();
        }
    }

    @GetMapping("/payment")
    public String payment(@RequestParam Long bookingId) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        Optional<Booking> opt = bookingService.getBookingByIdAndUser(bookingId, user.getUserId());
        if (opt.isEmpty() || opt.get().getStatus() != Booking.BookingStatus.pending) {
            return "redirect:/bookings";
        }
        Booking booking = opt.get();
        String checkoutUrl = payOSService.createPaymentLink(booking, null, null);
        return "redirect:" + checkoutUrl;
    }

    @GetMapping("/success")
    public String success(@RequestParam Long bookingId, Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        Optional<Booking> opt = bookingService.getBookingByIdAndUser(bookingId, user.getUserId());
        if (opt.isEmpty()) {
            return "redirect:/bookings";
        }
        model.addAttribute("booking", opt.get());
        return "booking-success";
    }

    @GetMapping("/cancel")
    public String cancel(Model model) {
        model.addAttribute("cancelled", true);
        return "booking-cancel";
    }
}
