package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUserId(Long userId);
    
    boolean existsByEmail(String email);
}
