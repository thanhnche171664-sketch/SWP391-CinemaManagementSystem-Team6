package com.swp391.team6.cinema.security;

import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

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
                newUser.setStatus(User.UserStatus.active);
                newUser.setIsVerify(true);
                return userRepository.save(newUser);
            });

            if (!user.getIsVerify()) {
                user.setIsVerify(true);
                userRepository.save(user);
            }

            if (user.getStatus() != User.UserStatus.active) {
                getRedirectStrategy().sendRedirect(request, response, "/login?error=account_inactive");
                return;
            }

            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("role", user.getRole().name());

            User sessionUser = copyUserForSession(user);
            CustomUserDetails userDetails = new CustomUserDetails(sessionUser);
            var newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(newAuth);

            String targetUrl = "/home";
            switch (user.getRole()) {
                case ADMIN:    targetUrl = "/admin"; break;
                case MANAGER:  targetUrl = "/manager"; break;
                case STAFF:    targetUrl = "/staff"; break;
                case CUSTOMER: targetUrl = "/home"; break;
                default:       targetUrl = "/movies"; break;
            }

            log.info("OAuth2 success: user={}, role={}, redirecting to {}", emailFinal, user.getRole(), targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 login success handler failed", e);
            getRedirectStrategy().sendRedirect(request, response, "/login?error=oauth_failed");
        }
    }

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
        copy.setIsVerify(user.getIsVerify());
        return copy;
    }
}