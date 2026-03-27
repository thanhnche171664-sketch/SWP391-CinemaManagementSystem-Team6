package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.MovieDTO;
import com.swp391.team6.cinema.dto.RevenueReportDTO;
import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.service.BookingService;
import com.swp391.team6.cinema.service.MovieService;
import com.swp391.team6.cinema.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final MovieService movieService;
    private final ReportService reportService;
    private final BookingService bookingService;
    private final CinemaBranchRepository cinemaBranchRepository;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        RevenueReportDTO report = reportService.buildRevenueReport(user.getBranchId());
        List<?> recentBookings = bookingService.getBookingsByBranch(user.getBranchId());

        model.addAttribute("user", user);
        model.addAttribute("branchName", resolveBranchName(user));
        model.addAttribute("summary", report.getSummary());
        model.addAttribute("movieRows", report.getRevenueByMovie().stream().limit(5).toList());
        model.addAttribute("recentBookings", recentBookings.stream().limit(8).toList());
        return "manager-dashboard";
    }

    @GetMapping("/movies")
    public String managerMovies(HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "8") int size,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String hidden,
                                @RequestParam(required = false) String sort) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Movie.MovieStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = Movie.MovieStatus.valueOf(status.trim());
            } catch (IllegalArgumentException ignored) {
                statusFilter = null;
            }
        }

        Boolean hiddenFilter = null;
        if (hidden != null && !hidden.isBlank()) {
            if ("shown".equalsIgnoreCase(hidden)) {
                hiddenFilter = false;
            } else if ("hidden".equalsIgnoreCase(hidden)) {
                hiddenFilter = true;
            }
        }

        Page<MovieDTO> moviePage = movieService.getBranchMoviesPageWithFilters(
                user.getBranchId(),
                page,
                size,
                search,
                statusFilter,
                hiddenFilter,
                sort
        );

        model.addAttribute("movieList", moviePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", moviePage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("statusFilter", status == null ? "" : status);
        model.addAttribute("hiddenFilter", hidden == null ? "" : hidden);
        model.addAttribute("sortFilter", sort == null ? "" : sort);
        model.addAttribute("readOnlyMode", true);
        model.addAttribute("movieBasePath", "/manager/movies");
        model.addAttribute("newMovie", new MovieDTO());
        model.addAttribute("genreList", movieService.getAllGenres());
        model.addAttribute("branches", movieService.getActiveBranches());
        model.addAttribute("user", user);
        return "movie-management";
    }

    private User requireManager(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || (user.getRole() != User.UserRole.MANAGER && user.getRole() != User.UserRole.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return null;
        }
        if (user.getRole() == User.UserRole.MANAGER && user.getBranchId() == null) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản quản lý chưa có chi nhánh.");
            return null;
        }
        return user;
    }

    private String resolveBranchName(User user) {
        Long branchId = user.getBranchId();
        if (branchId == null) {
            return "Toàn hệ thống";
        }
        return cinemaBranchRepository.findById(branchId)
                .map(b -> b.getBranchName() != null ? b.getBranchName() : "Chi nhánh #" + branchId)
                .orElse("Chi nhánh #" + branchId);
    }
}
