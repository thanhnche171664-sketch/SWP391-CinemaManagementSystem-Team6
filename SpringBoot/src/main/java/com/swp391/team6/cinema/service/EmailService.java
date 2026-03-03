package com.swp391.team6.cinema.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url}")
    private String appUrl;

    public void sendVerificationEmail(String toEmail, String fullName, String token) throws MessagingException {
        String verificationUrl = appUrl + "/auth/verify?token=" + token;
        
        String subject = "Verify Your Cinema Management Account";
        String htmlContent = buildVerificationEmailContent(fullName, verificationUrl);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }
    
    public void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        sendHtmlEmail(to, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildVerificationEmailContent(String fullName, String verificationUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 40px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #4A90E2 0%, #7B68EE 100%); padding: 30px; text-align: center; color: white; }" +
                ".header h1 { margin: 0; font-size: 28px; }" +
                ".content { padding: 40px 30px; }" +
                ".content p { color: #333; line-height: 1.6; margin-bottom: 20px; }" +
                ".button { display: inline-block; padding: 15px 40px; background: #00A9FF; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }" +
                ".button:hover { background: #0098e6; }" +
                ".footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 12px; }" +
                ".warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; color: #856404; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Welcome to Cinema Management!</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Hello " + fullName + ",</h2>" +
                "<p>Thank you for registering with Cinema Management System. We're excited to have you on board!</p>" +
                "<p>To complete your registration and verify your email address, please click the button below:</p>" +
                "<div style='text-align: center;'>" +
                "<a href='" + verificationUrl + "' class='button'>Verify Email Address</a>" +
                "</div>" +
                "<div class='warning'>" +
                "<strong>⚠️ Important:</strong> This verification link will expire in 24 hours." +
                "</div>" +
                "<p>If the button doesn't work, copy and paste this link into your browser:</p>" +
                "<p style='word-break: break-all; color: #00A9FF;'>" + verificationUrl + "</p>" +
                "<p>If you didn't create an account with Cinema Management, please ignore this email.</p>" +
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
