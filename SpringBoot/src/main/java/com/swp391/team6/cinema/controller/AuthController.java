package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        addCsrfToModel(request, model);
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(HttpServletRequest request, Model model) {
        addCsrfToModel(request, model);
        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(HttpServletRequest request, Model model) {
        addCsrfToModel(request, model);
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model, RedirectAttributes redirectAttributes) {
        authService.requestPasswordReset(email);
        redirectAttributes.addFlashAttribute("message", "Nếu email tồn tại, bạn sẽ nhận được link đặt lại mật khẩu.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, HttpServletRequest request, Model model) {
        if (token == null || token.isBlank()) {
            return "redirect:/forgot-password";
        }
        addCsrfToModel(request, model);
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String passwordConfirm,
                                Model model) {
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp.");
            model.addAttribute("token", token);
            return "reset-password";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Mật khẩu cần ít nhất 6 ký tự.");
            model.addAttribute("token", token);
            return "reset-password";
        }
        try {
            authService.resetPassword(token, password);
            return "redirect:/login?reset=1";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        }
    }

    private void addCsrfToModel(HttpServletRequest request, Model model) {
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            model.addAttribute("_csrf", csrf);
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(required = false) String phone,
                           Model model) {
        try {
            authService.register(fullName, email, password, phone);
            return "redirect:/register-success";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/register-success")
    public String registerSuccess() {
        return "register-success";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, RedirectAttributes redirectAttributes) {
        try {
            authService.verifyEmail(token);
            redirectAttributes.addAttribute("verified", "1");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addAttribute("error", "invalid_token");
            return "redirect:/login";
        }
    }
}
