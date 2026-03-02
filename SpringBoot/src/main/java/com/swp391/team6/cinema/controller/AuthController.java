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
