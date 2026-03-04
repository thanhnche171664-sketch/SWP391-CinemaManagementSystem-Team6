package com.swp391.team6.cinema.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Log mọi request tới OAuth2 để debug (xem request có tới server không).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OAuth2RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuth2RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.contains("oauth2") || uri.contains("login/oauth2")) {
            log.warn(">>> OAuth2 REQUEST: {} {}", request.getMethod(), uri);
        }
        filterChain.doFilter(request, response);
    }
}
