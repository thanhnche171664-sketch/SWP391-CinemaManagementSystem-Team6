package com.swp391.team6.cinema.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Keep DB enum values aligned with Payment.PaymentStatus.
     * Existing databases may still have only ('success','failed').
     */
    @PostConstruct
    public void ensurePaymentStatusEnumValues() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE payments " +
                            "MODIFY COLUMN payment_status " +
                            "ENUM('pending','success','failed','cancelled') " +
                            "NOT NULL DEFAULT 'pending'"
            );
        } catch (Exception ex) {
            log.warn("Skip payment_status enum migration: {}", ex.getMessage());
        }
    }
}
