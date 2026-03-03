package com.swp391.team6.cinema.security;

import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        // Get user email from OAuth2 user
        String email = oauth2User.getAttribute("email");
        
        // Find user in database
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null) {
            // Store user info in session
            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("role", user.getRole().name());
            
            // Redirect based on role
            switch (user.getRole()) {
                case ADMIN:
                    response.sendRedirect("/admin");
                    break;
                case MANAGER:
                    response.sendRedirect("/manager");
                    break;
                case STAFF:
                    response.sendRedirect("/staff");
                    break;
                case CUSTOMER:
                    response.sendRedirect("/home");
                    break;
                default:
                    response.sendRedirect("/");
            }
        } else {
            // This should not happen as user is created in CustomOAuth2UserService
            response.sendRedirect("/auth/login?error=oauth_failed");
        }
    }
}
