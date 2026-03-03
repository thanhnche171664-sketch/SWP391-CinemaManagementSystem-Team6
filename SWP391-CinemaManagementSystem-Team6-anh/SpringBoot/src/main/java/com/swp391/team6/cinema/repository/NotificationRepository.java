package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserUserId(Long userId);
    
    List<Notification> findByIsRead(Boolean isRead);
}
