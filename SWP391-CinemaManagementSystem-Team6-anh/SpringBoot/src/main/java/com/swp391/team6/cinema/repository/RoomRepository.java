package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByBranchBranchId(Long branchId);
    
    List<Room> findByStatus(Room.RoomStatus status);
}
