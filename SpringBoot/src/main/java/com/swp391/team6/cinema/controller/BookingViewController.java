package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.BookingListDTO;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.BookingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class BookingViewController {

    private final BookingService bookingService;

    @GetMapping("/history")
    public String viewMyHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/auth/login";
        }

        List<BookingListDTO> history = bookingService.getBookingHistoryByCustomer(user.getUserId());
        model.addAttribute("bookings", history);
        model.addAttribute("user", user);

        return "customer-history";
    }
}