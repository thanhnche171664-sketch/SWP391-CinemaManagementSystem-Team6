package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByBranchBranchId(Long branchId);

    List<Room> findByRoomNameContainingIgnoreCaseAndBranch_BranchId(String keyword, Long branchId);
    
    List<Room> findByStatus(Room.RoomStatus status);

    List<Room> findByRoomNameContainingIgnoreCase(String keyword);

    boolean existsByRoomNameAndBranch_BranchId(String roomName, Long branchId); // SELECT COUNT(*) > 0 FROM rooms WHERE room_name = ? AND branch_id = ?
}
