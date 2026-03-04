package com.swp391.team6.cinema.security;

import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Sau khi đăng nhập Google thành công: tìm User theo email hoặc tạo mới (CUSTOMER),
 * rồi đặt lại Authentication với CustomUserDetails để app dùng thống nhất.
 * Dùng bản sao User không chứa relation lazy để session serialize được.
 */
@Component
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        setDefaultTargetUrl("/movies");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        try {
            log.info("OAuth2 success handler: principal type={}", authentication.getPrincipal().getClass().getSimpleName());
            if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
                super.onAuthenticationSuccess(request, response, authentication);
                return;
            }

            String email = oauth2User.getAttribute("email");
            if (email == null || email.isBlank()) {
                email = (String) oauth2User.getAttributes().get("email");
            }
            final String emailFinal = (email != null && !email.isBlank()) ? email : null;
            if (emailFinal == null) {
                log.warn("Google OAuth2: no email in attributes");
                getRedirectStrategy().sendRedirect(request, response, "/login?error=no_email");
                return;
            }

            User user = userRepository.findByEmail(emailFinal).orElseGet(() -> {
                User newUser = new User();
                newUser.setFullName(oauth2User.getAttribute("name"));
                newUser.setEmail(emailFinal);
                newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                newUser.setPhone("");
                newUser.setRole(User.UserRole.CUSTOMER);
                newUser.setBranchId(null);
                newUser.setStatus(User.UserStatus.active);
                newUser.setEmailVerified(true);
                return userRepository.save(newUser);
            });

            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                userRepository.save(user);
            }
            if (user.getStatus() != User.UserStatus.active) {
                getRedirectStrategy().sendRedirect(request, response, "/login?error=account_inactive");
                return;
            }

            User sessionUser = copyUserForSession(user);
            CustomUserDetails userDetails = new CustomUserDetails(sessionUser);
            var newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(newAuth);

            log.info("OAuth2 success: user id={}, email={}, redirecting to /movies", sessionUser.getUserId(), sessionUser.getEmail());
            super.onAuthenticationSuccess(request, response, newAuth);
        } catch (Exception e) {
            log.error("OAuth2 login success handler failed", e);
            getRedirectStrategy().sendRedirect(request, response, "/login?error=oauth_failed");
        }
    }

    /** Bản sao User không có relation (branch, bookings...) để session serialize được. */
    private User copyUserForSession(User user) {
        User copy = new User();
        copy.setUserId(user.getUserId());
        copy.setFullName(user.getFullName());
        copy.setEmail(user.getEmail());
        copy.setPasswordHash(user.getPasswordHash());
        copy.setPhone(user.getPhone());
        copy.setRole(user.getRole());
        copy.setBranchId(user.getBranchId());
        copy.setStatus(user.getStatus());
        copy.setEmailVerified(user.isEmailVerified());
        return copy;
    }
}
