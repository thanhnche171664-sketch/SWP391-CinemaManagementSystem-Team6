package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.RevenueReportDTO;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public String viewRevenueReport(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
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

        model.addAttribute("summary", report.getSummary());
        model.addAttribute("revenueByMovie", report.getRevenueByMovie());
        model.addAttribute("revenueByDate", report.getRevenueByDate());
        model.addAttribute("revenueByBranch", report.getRevenueByBranch());
        model.addAttribute("user", user);
        return "report-revenue";
    }

    private boolean isAdminOrManager(User user) {
        return user != null &&
                (user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.MANAGER);
    }
}
