package com.swp391.team6.cinema.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginFailureHandler.class);

    public OAuth2LoginFailureHandler() {
        setDefaultFailureUrl("/login?error=oauth_failed");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 login FAILED: {}", exception.getMessage());
        if (exception.getCause() != null) {
            log.error("OAuth2 login cause: ", exception.getCause());
        }
        if (exception.getMessage() != null && exception.getMessage().contains("redirect_uri")) {
            log.error(">>> Kiểm tra Google Cloud Console: Authorized redirect URIs phải là chính xác: http://localhost:8080/login/oauth2/code/google");
        }
        super.onAuthenticationFailure(request, response, exception);
    }
}