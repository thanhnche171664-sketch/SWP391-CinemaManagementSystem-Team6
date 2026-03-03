package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.RegisterRequest;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.entity.VerificationToken;
import com.swp391.team6.cinema.repository.UserRepository;
import com.swp391.team6.cinema.repository.VerificationTokenRepository;
import com.swp391.team6.cinema.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/auth/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String registerUser(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        // Validate form
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // Check if passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            return "auth/register";
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            model.addAttribute("error", "Email already registered");
            return "auth/register";
        }

        try {
            // Create new user
            User user = new User();
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setPhone(request.getPhone());
            user.setRole(User.UserRole.CUSTOMER); // Default role
            user.setStatus(User.UserStatus.active); // Active status
            user.setIsVerify(false); // Not verified yet

            User savedUser = userRepository.save(user);

            // Generate verification token
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken(token, savedUser);
            tokenRepository.save(verificationToken);

            // Send verification email
            emailService.sendVerificationEmail(
                    savedUser.getEmail(),
                    savedUser.getFullName(),
                    token
            );

            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Please check your email to verify your account.");
            return "redirect:/auth/login";

        } catch (MessagingException e) {
            model.addAttribute("error", "Failed to send verification email. Please try again.");
            return "auth/register";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Please try again.");
            return "auth/register";
        }
    }

    @GetMapping("/auth/verify")
    public String verifyEmail(
            @RequestParam("token") String token,
            RedirectAttributes redirectAttributes
    ) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElse(null);

        if (verificationToken == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid verification token");
            return "redirect:/auth/login";
        }

        if (verificationToken.isExpired()) {
            redirectAttributes.addFlashAttribute("error", "Verification token has expired");
            return "redirect:/auth/login";
        }

        // Verify user
        User user = verificationToken.getUser();
        user.setIsVerify(true);
        userRepository.save(user);

        // Delete used token
        tokenRepository.delete(verificationToken);

        redirectAttributes.addFlashAttribute("success",
                "Email verified successfully! You can now login.");
        return "redirect:/auth/login";
    }
}
