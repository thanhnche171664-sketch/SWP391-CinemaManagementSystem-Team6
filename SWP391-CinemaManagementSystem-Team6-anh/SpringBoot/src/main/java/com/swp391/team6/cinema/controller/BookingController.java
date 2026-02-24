package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.booking.CreateBookingRequest;
import com.swp391.team6.cinema.dto.booking.PaymentInitResponse;
import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.BookingSeatRepository;
import com.swp391.team6.cinema.repository.SeatRepository;
import com.swp391.team6.cinema.repository.ShowtimeRepository;
import com.swp391.team6.cinema.service.BookingService;
import com.swp391.team6.cinema.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    @GetMapping("/booking/{showtimeId}")
    public String showBookingPage(@PathVariable Long showtimeId,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() == null || user.getRole() == User.UserRole.GUEST) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đặt vé!");
            return "redirect:/auth/login";
        }

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        Room room = showtime.getRoom();
        List<Seat> seats = seatRepository.findByRoomRoomId(room.getRoomId());
        List<Long> seatIds = seats.stream().map(Seat::getSeatId).toList();

        List<Booking.BookingStatus> lockStatuses = Arrays.asList(
                Booking.BookingStatus.pending,
                Booking.BookingStatus.paid
        );

        Set<Long> bookedSeatIds = bookingSeatRepository
                .findBySeatSeatIdInAndBookingShowtimeShowtimeIdAndBookingStatusIn(
                        seatIds,
                        showtimeId,
                        lockStatuses
                )
                .stream()
                .map(bs -> bs.getSeat().getSeatId())
                .collect(Collectors.toSet());

        model.addAttribute("showtime", showtime);
        model.addAttribute("movie", showtime.getMovie());
        model.addAttribute("room", room);
        model.addAttribute("branch", room.getBranch());
        model.addAttribute("seats", seats);
        model.addAttribute("bookedSeatIds", bookedSeatIds);
        model.addAttribute("user", user);

        return "booking";
    }

    @PostMapping("/booking/{showtimeId}")
    public String createBooking(@PathVariable Long showtimeId,
                                @RequestParam(name = "seatIds", required = false) List<Long> seatIds,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() == null || user.getRole() == User.UserRole.GUEST) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đặt vé!");
            return "redirect:/auth/login";
        }

        if (seatIds == null || seatIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một ghế.");
            return "redirect:/booking/" + showtimeId;
        }

        try {
            CreateBookingRequest request = new CreateBookingRequest(showtimeId, seatIds, "ONLINE");
            Booking booking = bookingService.createPendingBooking(user, request);
            PaymentInitResponse payment = paymentService.initOnlinePayment(booking);
            return "redirect:" + payment.getPaymentUrl();
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/booking/" + showtimeId;
        }
    }

    @GetMapping("/booking/confirmation")
    public String bookingConfirmation(@RequestParam(name = "bookingId", required = false) Long bookingId,
                                      @RequestParam(name = "status", required = false) String status,
                                      HttpSession session,
                                      Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", user);
        model.addAttribute("status", status);

        if (bookingId != null && user != null) {
            bookingService.findByIdForUser(bookingId, user)
                    .ifPresent(booking -> model.addAttribute("booking",
                            bookingService.toSummaryDto(booking)));
        }

        return "booking-confirmation";
    }
}

