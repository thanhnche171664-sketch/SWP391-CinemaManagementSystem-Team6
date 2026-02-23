package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.LoginRequest;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
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
                       HttpServletRequest request,
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
        
        System.out.println("[DEBUG] Login successful - User: " + user.getEmail() + ", Role: " + user.getRole());
        
        // *** QUAN TRỌNG: Authenticate vào Spring Security VÀ LƯU VÀO SESSION ***
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            user.getEmail(),
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        
        // Create security context and set authentication
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        // *** CRITICAL: Save SecurityContext to session ***
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        
        System.out.println("[DEBUG] Spring Security authentication set and saved to session for: " + user.getEmail());
        
        redirectAttributes.addFlashAttribute("success", message);
        
        // Redirect theo role
        String redirectUrl;
        switch (user.getRole()) {
            case ADMIN:
                redirectUrl = "redirect:/admin/dashboard";
                System.out.println("[DEBUG] Redirecting ADMIN to: /admin/dashboard");
                break;
            case MANAGER:
                redirectUrl = "redirect:/manager";
                System.out.println("[DEBUG] Redirecting MANAGER to: /manager");
                break;
            case STAFF:
                redirectUrl = "redirect:/staff";
                System.out.println("[DEBUG] Redirecting STAFF to: /staff");
                break;
            case CUSTOMER:
                redirectUrl = "redirect:/home";
                System.out.println("[DEBUG] Redirecting CUSTOMER to: /home");
                break;
            default:
                redirectUrl = "redirect:/";
                System.out.println("[DEBUG] Redirecting to: /");
        }
        
        return redirectUrl;
    }

    /**
     * Đăng xuất
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        // Clear Spring Security context
        SecurityContextHolder.clearContext();
        
        // Invalidate session
        session.invalidate();
        
        redirectAttributes.addFlashAttribute("success", "Đăng xuất thành công!");
        return "redirect:/auth/login";
    }
}
