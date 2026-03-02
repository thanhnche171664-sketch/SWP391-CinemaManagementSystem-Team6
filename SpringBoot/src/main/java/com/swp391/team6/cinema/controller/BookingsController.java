package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.security.CustomUserDetails;
import com.swp391.team6.cinema.service.BookingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/bookings")
public class BookingsController {

    private final BookingService bookingService;

    public BookingsController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUser();
        }
        return null;
    }

    @GetMapping
    public String list(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        List<Booking> bookings = bookingService.getBookingsByUser(user.getUserId());
        model.addAttribute("bookings", bookings);
        return "bookings";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        Optional<Booking> opt = bookingService.getBookingByIdAndUser(id, user.getUserId());
        if (opt.isEmpty()) {
            return "redirect:/bookings";
        }
        model.addAttribute("booking", opt.get());
        return "booking-detail";
    }
}
