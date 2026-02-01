package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findByEmailAndCodeAndIsUsedFalse(String email, String code);
    void deleteByEmail(String email);
    void deleteByExpiryTimeBefore(LocalDateTime dateTime);
}
