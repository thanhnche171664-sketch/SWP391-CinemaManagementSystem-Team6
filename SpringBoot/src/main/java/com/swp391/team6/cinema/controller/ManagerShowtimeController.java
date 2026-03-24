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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/manager/showtimes")
@RequiredArgsConstructor
public class ManagerShowtimeController {

    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final RoomService roomService;
    private final CinemaBranchRepository branchRepository;

    @GetMapping
    public String showtimePage(@RequestParam(required = false) String date,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        List<Showtime> showtimes;
        if (date != null && !date.isEmpty()) {
            showtimes = showtimeService.getByDateAndBranch(LocalDate.parse(date), user.getBranchId());
        } else {
            showtimes = showtimeService.getByBranch(user.getBranchId());
        }

        model.addAttribute("branches", resolveManagerBranches(user.getBranchId()));
        model.addAttribute("showtimes", showtimes);
        model.addAttribute("date", date);
        model.addAttribute("branchId", user.getBranchId());
        model.addAttribute("selectedBranchId", user.getBranchId());
        model.addAttribute("showtimeBasePath", "/manager/showtimes");
        model.addAttribute("roomsBasePath", "/manager/rooms/by-branch");
        model.addAttribute("isManager", true);

        return "showtime-list";
    }

    @GetMapping("/create")
    public String showCreatePage(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("branches", resolveManagerBranches(user.getBranchId()));
        model.addAttribute("rooms", new ArrayList<>());
        model.addAttribute("selectedBranchId", user.getBranchId());
        model.addAttribute("showtimeBasePath", "/manager/showtimes");
        model.addAttribute("roomsBasePath", "/manager/rooms/by-branch");
        model.addAttribute("isManager", true);

        return "showtime-create";
    }

    @GetMapping("/rooms/by-branch")
    @ResponseBody
    public List<Room> getRoomsByBranch(@RequestParam Long branchId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.MANAGER || user.getBranchId() == null) {
            return List.of();
        }
        if (!user.getBranchId().equals(branchId)) {
            return List.of();
        }
        return roomService.getRoomsByBranch(branchId);
    }

    @PostMapping("/create")
    public String createShowtime(@RequestParam Long movieId,
                                 @RequestParam Long roomId,
                                 @RequestParam String startTime,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        User user = requireManager(session, ra);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            Room room = roomService.getRoomById(roomId);
            if (room.getBranch() == null || !room.getBranch().getBranchId().equals(user.getBranchId())) {
                throw new RuntimeException("Bạn không có quyền tạo suất chiếu cho chi nhánh này.");
            }

            LocalDateTime time = LocalDateTime.parse(startTime);
            showtimeService.createShowtime(movieId, roomId, time);

            ra.addFlashAttribute("success", "Tạo thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/manager/showtimes/create";
        }

        return "redirect:/manager/showtimes";
    }

    @GetMapping("/edit/{id}")
    public String showEditPage(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Showtime showtime = showtimeService.getByIdWithDetails(id);
        if (!belongsToBranch(showtime, user.getBranchId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền cập nhật suất chiếu này.");
            return "redirect:/manager/showtimes";
        }

        model.addAttribute("showtime", showtime);
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("branches", resolveManagerBranches(user.getBranchId()));
        model.addAttribute("selectedBranchId", user.getBranchId());
        model.addAttribute("showtimeBasePath", "/manager/showtimes");
        model.addAttribute("roomsBasePath", "/manager/rooms/by-branch");
        model.addAttribute("isManager", true);

        return "showtime-edit";
    }

    @PostMapping("/update")
    public String updateShowtime(@RequestParam Long showtimeId,
                                 @RequestParam Long movieId,
                                 @RequestParam Long roomId,
                                 @RequestParam String startTime,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        User user = requireManager(session, ra);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            Showtime showtime = showtimeService.getByIdWithDetails(showtimeId);
            if (!belongsToBranch(showtime, user.getBranchId())) {
                throw new RuntimeException("Bạn không có quyền cập nhật suất chiếu này.");
            }
            LocalDateTime time = LocalDateTime.parse(startTime);
            showtimeService.updateShowtime(showtimeId, movieId, roomId, time);
            ra.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/manager/showtimes/edit/" + showtimeId;
        }

        return "redirect:/manager/showtimes";
    }

    private User requireManager(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.MANAGER) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return null;
        }
        if (user.getBranchId() == null) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản quản lý chưa có chi nhánh.");
            return null;
        }
        return user;
    }

    private boolean belongsToBranch(Showtime showtime, Long branchId) {
        return showtime.getRoom() != null
                && showtime.getRoom().getBranch() != null
                && branchId != null
                && branchId.equals(showtime.getRoom().getBranch().getBranchId());
    }

    private List<CinemaBranch> resolveManagerBranches(Long branchId) {
        return branchRepository.findById(branchId)
                .map(List::of)
                .orElseGet(List::of);
    }
}
