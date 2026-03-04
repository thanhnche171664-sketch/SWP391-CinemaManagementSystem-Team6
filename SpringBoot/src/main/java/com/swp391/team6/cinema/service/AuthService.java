package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.verification-token-expiry-hours:24}")
    private int verificationTokenExpiryHours;

    @Value("${app.password-reset-token-expiry-hours:1}")
    private int passwordResetTokenExpiryHours;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void register(String fullName, String email, String password, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhone(phone != null ? phone : "");
        user.setRole(User.UserRole.CUSTOMER);
        user.setBranchId(null);
        user.setStatus(User.UserStatus.active);
        user.setEmailVerified(false);
        String token = UUID.randomUUID().toString().replace("-", "");
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(verificationTokenExpiryHours));

        userRepository.save(user);

        try {
            emailService.sendVerificationEmail(email, token, fullName);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email. Please try again later.", e);
        }
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        if (user.getVerificationTokenExpiry() != null && user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            return;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(passwordResetTokenExpiryHours));
        userRepository.save(user);
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token, user.getFullName());
        } catch (Exception ignored) {
        }
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."));
        if (user.getPasswordResetTokenExpiry() != null && user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link đặt lại mật khẩu đã hết hạn. Vui lòng yêu cầu gửi lại.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }
}
