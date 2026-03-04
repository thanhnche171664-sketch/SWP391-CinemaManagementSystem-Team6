package com.swp391.team6.cinema.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String token, String userName) throws MessagingException {
        String verificationUrl = baseUrl + "/verify-email?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject("Verify your email - CinemaHub");
        helper.setText(
                "<p>Hello " + (userName != null && !userName.isBlank() ? userName : "there") + ",</p>" +
                "<p>Please click the link below to verify your email address:</p>" +
                "<p><a href=\"" + verificationUrl + "\">" + verificationUrl + "</a></p>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>If you did not register, please ignore this email.</p>",
                true
        );

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String token, String userName) throws MessagingException {
        String resetUrl = baseUrl + "/reset-password?token=" + token;
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject("Đặt lại mật khẩu - CinemaHub");
        helper.setText(
                "<p>Xin chào " + (userName != null && !userName.isBlank() ? userName : "bạn") + ",</p>" +
                "<p>Bạn đã yêu cầu đặt lại mật khẩu. Nhấn vào link bên dưới để đặt mật khẩu mới:</p>" +
                "<p><a href=\"" + resetUrl + "\">" + resetUrl + "</a></p>" +
                "<p>Link có hiệu lực trong 1 giờ. Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>",
                true
        );
        mailSender.send(message);
    }
}
