package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.RevenueReportDTO;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private static final int PAGE_SIZE = 5;

    @GetMapping
    public String viewRevenueReport(HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes,
                                    @RequestParam(defaultValue = "0") int moviePage,
                                    @RequestParam(defaultValue = "0") int datePage,
                                    @RequestParam(defaultValue = "0") int branchPage) {
        User user = (User) session.getAttribute("loggedInUser");
        if (!isAdminOrManager(user)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }

        if (user.getRole() == User.UserRole.MANAGER && user.getBranchId() == null) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản quản lý chưa có chi nhánh.");
            return "redirect:/auth/login";
        }

        Long branchId = user.getRole() == User.UserRole.MANAGER ? user.getBranchId() : null;
        RevenueReportDTO report = reportService.buildRevenueReport(branchId);

        PageSlice<?> movieSlice = paginate(report.getRevenueByMovie(), moviePage);
        PageSlice<?> dateSlice = paginate(report.getRevenueByDate(), datePage);
        PageSlice<?> branchSlice = paginate(report.getRevenueByBranch(), branchPage);

        model.addAttribute("summary", report.getSummary());
        model.addAttribute("revenueByMovie", movieSlice.items);
        model.addAttribute("revenueByDate", dateSlice.items);
        model.addAttribute("revenueByBranch", branchSlice.items);
        model.addAttribute("moviePage", movieSlice.currentPage);
        model.addAttribute("datePage", dateSlice.currentPage);
        model.addAttribute("branchPage", branchSlice.currentPage);
        model.addAttribute("movieTotalPages", movieSlice.totalPages);
        model.addAttribute("dateTotalPages", dateSlice.totalPages);
        model.addAttribute("branchTotalPages", branchSlice.totalPages);
        model.addAttribute("user", user);
        return "report-revenue";
    }

    private <T> PageSlice<T> paginate(List<T> source, int requestedPage) {
        List<T> safeSource = source == null ? Collections.emptyList() : source;
        if (safeSource.isEmpty()) {
            return new PageSlice<>(Collections.emptyList(), 0, 1);
        }

        int totalPages = (int) Math.ceil((double) safeSource.size() / PAGE_SIZE);
        int currentPage = Math.max(0, Math.min(requestedPage, totalPages - 1));
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, safeSource.size());
        return new PageSlice<>(safeSource.subList(start, end), currentPage, totalPages);
    }

    private static class PageSlice<T> {
        private final List<T> items;
        private final int currentPage;
        private final int totalPages;

        private PageSlice(List<T> items, int currentPage, int totalPages) {
            this.items = items;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
        }
    }

    private boolean isAdminOrManager(User user) {
        return user != null &&
                (user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.MANAGER);
    }
}
