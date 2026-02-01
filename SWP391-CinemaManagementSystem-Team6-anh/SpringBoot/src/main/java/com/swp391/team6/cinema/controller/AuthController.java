package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.LoginRequest;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * Hiển thị trang login
     */
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login"; // Trả về auth/login.html
    }

    /**
     * Xử lý đăng nhập
     */
    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest, 
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        
        Map<String, Object> result = userService.authenticateUser(
            loginRequest.getEmail(), 
            loginRequest.getPassword()
        );
        
        boolean success = (boolean) result.get("success");
        String message = (String) result.get("message");
        
        if (!success) {
            // Đăng nhập thất bại - hiển thị lỗi
            redirectAttributes.addFlashAttribute("error", message);
            redirectAttributes.addFlashAttribute("email", loginRequest.getEmail());
            return "redirect:/auth/login";
        }
        
        // Đăng nhập thành công - lưu user vào session
        User user = (User) result.get("user");
        session.setAttribute("loggedInUser", user);
        session.setAttribute("userRole", user.getRole().toString());
        
        redirectAttributes.addFlashAttribute("success", message);
        
        // Redirect theo role
        switch (user.getRole()) {
            case ADMIN:
                return "redirect:/admin";
            case MANAGER:
                return "redirect:/manager";
            case STAFF:
                return "redirect:/staff";
            case CUSTOMER:
                return "redirect:/home";
            default:
                return "redirect:/";
        }
    }

    /**
     * Đăng xuất
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đăng xuất thành công!");
        return "redirect:/auth/login";
    }
}
