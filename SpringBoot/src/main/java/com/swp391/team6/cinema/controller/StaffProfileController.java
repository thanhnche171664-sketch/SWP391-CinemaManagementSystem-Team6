package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/profile")
public class StaffProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("loggedInUser");

        if (sessionUser == null) return "redirect:/auth/login";

        User currentUser = userService.getUserById(sessionUser.getUserId());

        model.addAttribute("user", currentUser);
        return "staff/profile";
    }

    @PostMapping("/change-password")
    public String handlePasswordChange(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes ra) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/auth/login";

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu xác nhận không trùng khớp!");
            return "redirect:/staff/profile";
        }

        try {
            boolean success = userService.changePassword(user.getUserId(), oldPassword, newPassword);
            if (success) {
                ra.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            } else {
                ra.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác!");
            }
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/staff/profile";
    }

    @PostMapping("/update-info")
    public String updateProfileInfo(
            @RequestParam String fullName,
            @RequestParam String phone,
            HttpSession session,
            RedirectAttributes ra) {

        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return "redirect:/auth/login";

        try {
            userService.updateBasicInfo(sessionUser.getUserId(), fullName, phone);

            sessionUser.setFullName(fullName);
            sessionUser.setPhone(phone);
            session.setAttribute("loggedInUser", sessionUser);

            ra.addFlashAttribute("success", "Cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/staff/profile";
    }
}