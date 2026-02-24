package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.OtpCode;
import com.swp391.team6.cinema.repository.OtpCodeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate a 6-digit OTP code
     */
    public String generateOtpCode() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Create and send OTP to email
     */
    @Transactional
    public void sendOtp(String email) throws MessagingException {
        // Delete old OTPs for this email
        otpCodeRepository.deleteByEmail(email);

        // Generate new OTP
        String otpCode = generateOtpCode();
        OtpCode otp = new OtpCode(email, otpCode);
        otpCodeRepository.save(otp);

        // Send email
        sendOtpEmail(email, otpCode);
    }

    /**
     * Verify OTP code
     */
    public boolean verifyOtp(String email, String code) {
        Optional<OtpCode> otpOptional = otpCodeRepository.findByEmailAndCodeAndIsUsedFalse(email, code);

        if (otpOptional.isEmpty()) {
            return false;
        }

        OtpCode otp = otpOptional.get();

        // Check if expired
        if (otp.isExpired()) {
            return false;
        }

        // Mark as used
        otp.setIsUsed(true);
        otpCodeRepository.save(otp);

        return true;
    }

    /**
     * Clean up expired OTPs
     */
    @Transactional
    public void cleanupExpiredOtps() {
        otpCodeRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
    }

    /**
     * Send OTP email
     */
    private void sendOtpEmail(String toEmail, String otpCode) throws MessagingException {
        String subject = "Password Reset OTP - Cinema Management";
        String htmlContent = buildOtpEmailContent(otpCode);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Build OTP email HTML content
     */
    private String buildOtpEmailContent(String otpCode) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 40px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #4A90E2 0%, #7B68EE 100%); padding: 30px; text-align: center; color: white; }" +
                ".header h1 { margin: 0; font-size: 28px; }" +
                ".content { padding: 40px 30px; text-align: center; }" +
                ".otp-box { background: #f8f9fa; border: 2px dashed #00A9FF; border-radius: 10px; padding: 20px; margin: 30px 0; }" +
                ".otp-code { font-size: 48px; font-weight: bold; color: #00A9FF; letter-spacing: 10px; font-family: 'Courier New', monospace; }" +
                ".warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; color: #856404; text-align: left; }" +
                ".footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Password Reset Request</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Your OTP Code</h2>" +
                "<p>Use this code to reset your password:</p>" +
                "<div class='otp-box'>" +
                "<div class='otp-code'>" + otpCode + "</div>" +
                "</div>" +
                "<div class='warning'>" +
                "<strong>⏱️ Important:</strong> This OTP will expire in <strong>60 seconds</strong>." +
                "</div>" +
                "<p>If you didn't request a password reset, please ignore this email.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>© 2026 Cinema Management System. All rights reserved.</p>" +
                "<p>This is an automated email, please do not reply.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
