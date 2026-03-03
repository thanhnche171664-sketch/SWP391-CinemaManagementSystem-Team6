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
@RequestMapping("/employee/profile")
public class EmployeeProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("loggedInUser");

        if (sessionUser == null) return "redirect:/auth/login";

        User currentUser = userService.getUserById(sessionUser.getUserId());

        model.addAttribute("user", currentUser);
        return "employee/profile";
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
            return "redirect:/employee/profile";
        }

        boolean success = userService.changePassword(user.getUserId(), oldPassword, newPassword);

        if (success) {
            ra.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        } else {
            ra.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác!");
        }

        return "redirect:/employee/profile";
    }
}