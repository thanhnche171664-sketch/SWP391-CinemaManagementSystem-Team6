package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.BookingDetailDTO;
import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.SeatRepository;
import com.swp391.team6.cinema.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/staff/booking")
@RequiredArgsConstructor
public class StaffBookingController {

    private final StaffBookingService staffService;
    private final SeatRepository seatRepository;

    // 1. Trang chủ POS: Hiển thị danh sách phim đang chiếu
    @GetMapping
    public String index(Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("movies", staffService.getShowingMovies());
        return "staff/counter-booking";
    }

    // 2. Hiển thị danh sách suất chiếu theo phim
    @GetMapping("/showtimes/{movieId}")
    public String showShowtimes(@PathVariable Long movieId, Model model, HttpSession session) {
        User staff = (User) session.getAttribute("loggedInUser");
        if (staff == null) return "redirect:/auth/login";

        // Giả định staff có thông tin branchId
        model.addAttribute("showtimes", staffService.getShowtimes(movieId, staff.getBranchId()));
        return "staff/showtimes";
    }

    // 3. Hiển thị sơ đồ ghế
    @GetMapping("/seats/{showtimeId}")
    public String showSeatSelection(@PathVariable Long showtimeId, Model model) {
        Showtime showtime = staffService.findShowtimeById(showtimeId);
        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seatRepository.findByRoomRoomIdOrderBySeatRowAscSeatNumberAsc(showtime.getRoom().getRoomId()));
        model.addAttribute("occupiedIds", staffService.getOccupiedSeatIds(showtimeId));
        return "staff/seat-selection";
    }

    // 4. Xác nhận thông tin trước khi lưu vào DB
    @PostMapping("/booking/create")
    public String confirmBooking(@RequestParam Long showtimeId, @RequestParam List<Long> seatIds, Model model) {
        BookingDetailDTO bookingData = staffService.prepareBookingConfirmationDTO(showtimeId, seatIds);

        model.addAttribute("bookingData", bookingData);
        model.addAttribute("showtimeId", showtimeId); // CỰC KỲ QUAN TRỌNG
        model.addAttribute("seatIds", seatIds);       // CỰC KỲ QUAN TRỌNG

        return "staff/booking-confirmation";
    }

    // 5. Xử lý lưu booking và hiển thị kết quả (Hóa đơn)
    @PostMapping("/confirm")
    public String confirm(@RequestParam Long showtimeId, @RequestParam List<Long> seatIds,
                          @RequestParam String method, HttpSession session, Model model) {
        User staff = (User) session.getAttribute("loggedInUser");
        if (staff == null) return "redirect:/auth/login";

        // Thực hiện tạo booking và nhận về entity đã lưu
        Booking booking = staffService.createStaffBooking(showtimeId, seatIds, staff, method);

        // Chuyển sang DTO và đẩy vào Model để view hiển thị hóa đơn
        model.addAttribute("booking", staffService.mapToDTO(booking));
        return "staff/booking-success";
    }
}