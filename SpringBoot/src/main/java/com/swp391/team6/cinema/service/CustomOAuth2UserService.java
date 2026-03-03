package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.entity.User.UserRole;
import com.swp391.team6.cinema.entity.User.UserStatus;
import com.swp391.team6.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&";
    private static final SecureRandom random = new SecureRandom();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        // Get email from OAuth2 user info
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        // Check if user exists
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            // Generate random password for new Google user
            String randomPassword = generateRandomPassword(12);
            
            // Create new user if not exists
            user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setPasswordHash(passwordEncoder.encode(randomPassword));
            user.setRole(UserRole.CUSTOMER);
            user.setStatus(UserStatus.active);
            user.setIsVerify(true); // Google users are pre-verified
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            // Send password to user's email
            sendPasswordEmail(email, name, randomPassword);
        } else {
            // Update user info if exists
            if (!user.getIsVerify()) {
                user.setIsVerify(true); // Verify user if they login via Google
                userRepository.save(user);
            }
        }
        
        // Return OAuth2User with authorities
        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
            oauth2User.getAttributes(),
            "email"
        );
    }
    
    private String generateRandomPassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }
    
    private void sendPasswordEmail(String email, String name, String password) {
        try {
            String subject = "Welcome to Cinema Management - Your Account Password";
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #4A90E2 0%%, #7B68EE 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .password-box { background: white; border: 2px solid #4A90E2; padding: 20px; margin: 20px 0; border-radius: 8px; text-align: center; }
                        .password { font-size: 24px; font-weight: bold; color: #4A90E2; letter-spacing: 2px; font-family: monospace; }
                        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎬 Welcome to Cinema Management!</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s!</h2>
                            <p>Thank you for signing up with Google. Your account has been created successfully.</p>
                            
                            <p>We've generated a secure password for your account. You can use this password to login directly (without Google) anytime:</p>
                            
                            <div class="password-box">
                                <div style="color: #666; font-size: 14px; margin-bottom: 10px;">Your Password</div>
                                <div class="password">%s</div>
                            </div>
                            
                            <div class="warning">
                                <strong>⚠️ Security Tip:</strong> Please change this password after your first login for better security.
                            </div>
                            
                            <p><strong>Account Details:</strong></p>
                            <ul>
                                <li>Email: %s</li>
                                <li>Role: Customer</li>
                                <li>Status: Active & Verified</li>
                            </ul>
                            
                            <p>You can now:</p>
                            <ul>
                                <li>Login with Google (recommended)</li>
                                <li>Login with email and password above</li>
                                <li>Browse and book movie tickets</li>
                                <li>Manage your bookings</li>
                            </ul>
                            
                            <div class="footer">
                                <p>This is an automated email. Please do not reply.</p>
                                <p>&copy; 2026 Cinema Management System. All rights reserved.</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """, name, password, email);
            
            emailService.sendEmail(email, subject, htmlContent);
        } catch (Exception e) {
            // Log error but don't fail the authentication
            System.err.println("Failed to send password email to " + email + ": " + e.getMessage());
        }
    }
}
