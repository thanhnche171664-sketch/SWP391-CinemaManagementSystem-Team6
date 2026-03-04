package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.StaffDTO;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/staff")
public class StaffController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String showStaffPage(@RequestParam(defaultValue = "0") int page,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || (user.getRole() != User.UserRole.ADMIN && user.getRole() != User.UserRole.MANAGER)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }

        int pageSize = 10;
        Page<StaffDTO> staffPage = userService.findStaffPaged(page, pageSize);

        model.addAttribute("staffList", staffPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", staffPage.getTotalPages());
        model.addAttribute("totalItems", staffPage.getTotalElements());

        model.addAttribute("branches", userService.findAllBranches());
        model.addAttribute("newStaff", new StaffDTO());
        model.addAttribute("user", user);

        return "staff-management";
    }

    @PostMapping("/save")
    public String saveStaff(@ModelAttribute("newStaff") StaffDTO staffDTO,
                            RedirectAttributes redirectAttributes) {
        try {
            userService.saveStaff(staffDTO);
            // Thông báo thành công
            redirectAttributes.addFlashAttribute("success", "Lưu thông tin nhân viên thành công!");
        } catch (RuntimeException e) {
            // Bắt lỗi từ Service (trùng email, phone, định dạng sai...)
            // và gửi thông báo lỗi về trang quản lý
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            // Các lỗi hệ thống không mong muốn khác
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại!");
        }
        return "redirect:/admin/staff";
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái tài khoản.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật trạng thái.");
        }
        return "redirect:/admin/staff";
    }

    @GetMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteStaff(id);
            redirectAttributes.addFlashAttribute("success", "Đã tạm khóa tài khoản nhân viên.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thực hiện thao tác này.");
        }
        return "redirect:/admin/staff";
    }
}