package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByBranchBranchId(Long branchId);

    List<Room> findByRoomNameContainingIgnoreCaseAndBranch_BranchId(String keyword, Long branchId);
    
    List<Room> findByStatus(Room.RoomStatus status);

    List<Room> findByRoomNameContainingIgnoreCase(String keyword);

    Page<Room> findByRoomNameContainingIgnoreCaseAndBranchBranchId(
            String keyword,
            Long branchId,
            Pageable pageable
    );

    Page<Room> findByRoomNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    Page<Room> findByBranchBranchId(
            Long branchId,
            Pageable pageable
    );

    boolean existsByRoomNameAndBranch_BranchId(String roomName, Long branchId); // SELECT COUNT(*) > 0 FROM rooms WHERE room_name = ? AND branch_id = ?
}
