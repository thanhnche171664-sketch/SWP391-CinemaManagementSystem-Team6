package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(Long userId);
    Optional<User> findByFullName(String fullName);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmailOrPhone(String email, String phone);
    boolean existsByEmail(String email);

    Page<User> findByRoleIn(Collection<User.UserRole> roles, Pageable pageable);

    List<User> findByRoleIn(List<User.UserRole> roles);

    Optional<User> findFirstByRole(User.UserRole role);

    List<User> findByFullNameContainingIgnoreCaseAndRole(String fullName, User.UserRole role);
}