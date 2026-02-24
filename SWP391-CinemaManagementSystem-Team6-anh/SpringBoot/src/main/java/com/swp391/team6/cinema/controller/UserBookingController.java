package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.booking.BookingSummaryDto;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.BookingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserBookingController {

    private final BookingService bookingService;

    @GetMapping("/my-bookings")
    public String listUserBookings(HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() == null || user.getRole() == User.UserRole.GUEST) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để xem lịch sử đặt vé!");
            return "redirect:/auth/login";
        }

        List<BookingSummaryDto> bookings = bookingService.getBookingsForUser(user);
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        return "my-bookings";
    }

    @GetMapping("/my-bookings/{id}")
    public String bookingDetail(@PathVariable("id") Long bookingId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() == null || user.getRole() == User.UserRole.GUEST) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để xem chi tiết đặt vé!");
            return "redirect:/auth/login";
        }

        return bookingService.findByIdForUser(bookingId, user)
                .map(booking -> {
                    model.addAttribute("user", user);
                    model.addAttribute("booking", bookingService.toSummaryDto(booking));
                    return "booking-detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt vé.");
                    return "redirect:/my-bookings";
                });
    }
}

