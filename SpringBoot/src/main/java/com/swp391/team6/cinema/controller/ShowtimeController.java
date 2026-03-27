package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.Room;
import com.swp391.team6.cinema.entity.Showtime;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.service.MovieService;
import com.swp391.team6.cinema.service.RoomService;
import com.swp391.team6.cinema.service.ShowtimeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Controller
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final RoomService roomService;
    private final CinemaBranchRepository branchRepository;


    @GetMapping("/admin/showtimes")
    public String showtimePage(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Showtime> showtimePage;
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        List<Showtime> showtimes;

        if (date != null && !date.isEmpty() && branchId != null) {
            showtimePage = showtimeService.getByDateAndBranch(
                    LocalDate.parse(date), branchId, pageable);
        }
        else if (date != null && !date.isEmpty()) {
            showtimePage = showtimeService.getByDate(
                    LocalDate.parse(date), pageable);
        }
        else if (branchId != null) {
            showtimePage = showtimeService.getByBranch(branchId, pageable);
        }
        else {
            showtimePage = showtimeService.getAll(pageable);
        }

        model.addAttribute("branches", branchRepository.findAll());

        model.addAttribute("showtimes", showtimePage.getContent());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", showtimePage.getTotalPages());

        model.addAttribute("date", date);
        model.addAttribute("branchId", branchId);
        model.addAttribute("showtimeBasePath", "/admin/showtimes");
        model.addAttribute("roomsBasePath", "/admin/rooms/by-branch");
        model.addAttribute("isManager", false);

        return "showtime-list";
    }

    @GetMapping("/admin/showtimes/create")
    public String showCreatePage(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("movies", movieService.getAllMovies());

        List<CinemaBranch> branches = branchRepository.findAll();
        model.addAttribute("branches", branches);

        model.addAttribute("rooms", new ArrayList<>());
        model.addAttribute("showtimeBasePath", "/admin/showtimes");
        model.addAttribute("roomsBasePath", "/admin/rooms/by-branch");
        model.addAttribute("isManager", false);

        return "showtime-create";
    }

    @GetMapping("/admin/rooms/by-branch")
    @ResponseBody
    public List<Room> getRoomsByBranch(@RequestParam Long branchId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return List.of();
        }
        return roomService.getRoomsByBranch(branchId);
    }

    @PostMapping("/admin/showtimes/create")
    public String createShowtime(
            @RequestParam Long movieId,
            @RequestParam Long roomId,
            @RequestParam String startTime,
            HttpSession session,
            RedirectAttributes ra
    ) {
        User user = requireAdmin(session, ra);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {

            if (movieId == null) {
                throw new RuntimeException("Vui lòng chọn phim");
            }

            if (roomId == null) {
                throw new RuntimeException("Vui lòng chọn phòng");
            }

            if (startTime == null || startTime.isBlank()) {
                throw new RuntimeException("Vui lòng chọn thời gian");
            }

            LocalDateTime time;
            try {
                time = LocalDateTime.parse(startTime);
            } catch (Exception e) {
                throw new RuntimeException("Thời gian không hợp lệ");
            }

            if (time.isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Không thể tạo suất chiếu trong quá khứ");
            }

            Room room = roomService.getById(roomId);
            if (room.getStatus() == Room.RoomStatus.inactive) {
                throw new RuntimeException("Phòng đang bị khóa");
            }

            showtimeService.createShowtime(movieId, roomId, time);

            ra.addFlashAttribute("success", "Tạo thành công!");

        } catch (Exception e) {

            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/showtimes/create";
        }

        return "redirect:/admin/showtimes";
    }

    @GetMapping("/admin/showtimes/{id}")
    @ResponseBody
    public Map<String, Object> getShowtimeById(@PathVariable Long id) {

        Showtime s = showtimeService.getById(id);

        Map<String, Object> data = new HashMap<>();
        data.put("showtimeId", s.getShowtimeId());
        data.put("status", s.getStatus().name());

        return data;
    }

    @GetMapping("/admin/showtimes/edit/{id}")
    public String showEditPage(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Showtime showtime = showtimeService.getByIdWithDetails(id);
        model.addAttribute("showtime", showtime);
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("branches", branchRepository.findAll());
        model.addAttribute("showtimeBasePath", "/admin/showtimes");
        model.addAttribute("roomsBasePath", "/admin/rooms/by-branch");
        model.addAttribute("isManager", false);

        return "showtime-edit";
    }

    @PostMapping("/admin/showtimes/update-status")
    @ResponseBody
    public String updateStatus(@RequestParam Long id,
                               @RequestParam String status) {

        showtimeService.updateStatus(id, status);
        return "OK";
    }

    @PostMapping("/admin/showtimes/update")
    public String updateShowtime(
            @RequestParam Long showtimeId,
            @RequestParam Long movieId,
            @RequestParam Long roomId,
            @RequestParam String startTime,
            HttpSession session,
            RedirectAttributes ra
    ) {
        User user = requireAdmin(session, ra);
        if (user == null) {
            return "redirect:/auth/login";
        }
        try {
            LocalDateTime time = LocalDateTime.parse(startTime);

            showtimeService.updateShowtime(showtimeId, movieId, roomId, time);

            ra.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/showtimes/edit/" + showtimeId;
        }

        return "redirect:/admin/showtimes";
    }

    private User requireAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return null;
        }
        return user;
    }
}