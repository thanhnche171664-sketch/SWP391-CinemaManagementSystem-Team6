package com.swp391.team6.cinema.config;

import com.swp391.team6.cinema.security.OAuth2LoginFailureHandler;
import com.swp391.team6.cinema.security.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oauth2LoginFailureHandler;

    public SecurityConfig(OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
                          OAuth2LoginFailureHandler oauth2LoginFailureHandler) {
        this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
        this.oauth2LoginFailureHandler = oauth2LoginFailureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/payment/payos-callback"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/movies", "/movies/**").permitAll()
                        .requestMatchers("/payment/payos-callback").permitAll()
                        .requestMatchers("/login", "/register", "/register-success", "/verify-email", "/forgot-password", "/reset-password").permitAll()
                        .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                        .requestMatchers("/favicon.ico", "/styles/**", "/images/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/booking/**", "/bookings", "/bookings/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/movies", true)
                        .failureUrl("/login?error")
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oauth2LoginSuccessHandler)
                        .failureHandler(oauth2LoginFailureHandler)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
