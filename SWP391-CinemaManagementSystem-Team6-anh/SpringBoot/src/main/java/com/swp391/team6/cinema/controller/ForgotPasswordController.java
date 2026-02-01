package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.UserRepository;
import com.swp391.team6.cinema.service.OtpService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    // Step 1: Show forgot password page (enter email)
    @GetMapping("/auth/forgot-password")
    public String showForgotPasswordPage() {
        return "auth/forgot-password";
    }

    // Step 2: Send OTP to email
    @PostMapping("/auth/forgot-password")
    public String sendOtp(
            @RequestParam("email") String email,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        // Check if email exists
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Email not found in our system");
            return "auth/forgot-password";
        }

        try {
            // Send OTP
            otpService.sendOtp(email);

            // Store email in session
            session.setAttribute("resetEmail", email);
            session.setMaxInactiveInterval(300); // 5 minutes session timeout

            redirectAttributes.addFlashAttribute("success", "OTP has been sent to your email");
            return "redirect:/auth/verify-otp";

        } catch (MessagingException e) {
            model.addAttribute("error", "Failed to send OTP. Please try again.");
            return "auth/forgot-password";
        }
    }

    // Step 3: Show OTP verification page
    @GetMapping("/auth/verify-otp")
    public String showVerifyOtpPage(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please start again.");
            return "redirect:/auth/forgot-password";
        }
        return "auth/verify-otp";
    }

    // Step 4: Verify OTP
    @PostMapping("/auth/verify-otp")
    public String verifyOtp(
            @RequestParam("otp") String otp,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please start again.");
            return "redirect:/auth/forgot-password";
        }

        // Verify OTP
        if (otpService.verifyOtp(email, otp)) {
            // OTP valid, proceed to reset password
            session.setAttribute("otpVerified", true);
            return "redirect:/auth/reset-password";
        } else {
            model.addAttribute("error", "Invalid or expired OTP");
            return "auth/verify-otp";
        }
    }

    // Resend OTP
    @PostMapping("/auth/resend-otp")
    public String resendOtp(
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please start again.");
            return "redirect:/auth/forgot-password";
        }

        try {
            otpService.sendOtp(email);
            redirectAttributes.addFlashAttribute("success", "New OTP has been sent");
            return "redirect:/auth/verify-otp";
        } catch (MessagingException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send OTP. Please try again.");
            return "redirect:/auth/verify-otp";
        }
    }

    // Step 5: Show reset password page
    @GetMapping("/auth/reset-password")
    public String showResetPasswordPage(HttpSession session, RedirectAttributes redirectAttributes) {
        Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");
        if (otpVerified == null || !otpVerified) {
            redirectAttributes.addFlashAttribute("error", "Please verify OTP first");
            return "redirect:/auth/verify-otp";
        }
        return "auth/reset-password";
    }

    // Step 6: Reset password
    @PostMapping("/auth/reset-password")
    public String resetPassword(
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");
        String email = (String) session.getAttribute("resetEmail");

        if (otpVerified == null || !otpVerified || email == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid session. Please start again.");
            return "redirect:/auth/forgot-password";
        }

        // Validate passwords
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "auth/reset-password";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters");
            return "auth/reset-password";
        }

        // Update password
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPasswordHash(passwordEncoder.encode(password));
            userRepository.save(user);

            // Clear session
            session.invalidate();

            redirectAttributes.addFlashAttribute("success", "Password reset successfully! You can now login.");
            return "redirect:/auth/login";
        } else {
            model.addAttribute("error", "User not found");
            return "auth/reset-password";
        }
    }
}
