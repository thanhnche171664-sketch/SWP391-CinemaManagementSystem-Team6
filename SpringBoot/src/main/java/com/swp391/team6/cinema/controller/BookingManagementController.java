package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.BookingDetailDTO;
import com.swp391.team6.cinema.dto.BookingListDTO;
import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.BookingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class BookingManagementController {

    private final BookingService bookingService;

    @GetMapping
    public String list(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (!isAdminOrManager(user)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }

        List<BookingListDTO> bookings;
        if (user.getRole() == User.UserRole.MANAGER) {
            if (user.getBranchId() == null) {
                redirectAttributes.addFlashAttribute("error", "Tài khoản quản lý chưa có chi nhánh.");
                return "redirect:/auth/login";
            }
            bookings = bookingService.getBookingsByBranch(user.getBranchId());
        } else {
            bookings = bookingService.getAllBookings();
        }

        long totalBookings = bookings.size();
        long paidBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.paid)
                .count();
        long pendingBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.pending)
                .count();
        long cancelledBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.cancelled)
                .count();

        model.addAttribute("bookingList", bookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("paidBookings", paidBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("user", user);
        return "booking-management";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (!isAdminOrManager(user)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }

        Optional<BookingDetailDTO> bookingDetail;
        if (user.getRole() == User.UserRole.MANAGER) {
            if (user.getBranchId() == null) {
                redirectAttributes.addFlashAttribute("error", "Tài khoản quản lý chưa có chi nhánh.");
                return "redirect:/auth/login";
            }
            bookingDetail = bookingService.getBookingDetailForBranch(id, user.getBranchId());
        } else {
            bookingDetail = bookingService.getBookingDetail(id);
        }

        if (bookingDetail.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt vé.");
            return "redirect:/admin/bookings";
        }

        model.addAttribute("booking", bookingDetail.get());
        model.addAttribute("user", user);
        return "booking-detail";
    }

    private boolean isAdminOrManager(User user) {
        return user != null &&
                (user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.MANAGER);
    }
}
