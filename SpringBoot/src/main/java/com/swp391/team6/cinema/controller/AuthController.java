package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.LoginRequest;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
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
    public String showLoginPage(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        if (redirect != null && !redirect.isBlank() && redirect.startsWith("/")) {
            model.addAttribute("redirect", redirect);
        }
        return "auth/login";
    }

    /**
     * Xử lý đăng nhập
     */
    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest,
                        @RequestParam(required = false) String redirect,
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

        // Đồng bộ SecurityContext để qua được Spring Security filter
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

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
                if (redirect != null && !redirect.isBlank() && redirect.startsWith("/"))
                    return "redirect:" + redirect;
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