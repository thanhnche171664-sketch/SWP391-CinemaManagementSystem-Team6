package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Room;
import com.swp391.team6.cinema.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }

    public List<Room> getRoomsByBranch(Long branchId) {
        return roomRepository.findByBranchBranchId(branchId);
    }

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public void deleteById(Long id) {
        roomRepository.deleteById(id);
    }

    public List<Room> searchRooms(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return roomRepository.findAll();
        }
        return roomRepository.findByRoomNameContainingIgnoreCase(keyword);
    }

    public boolean existsByRoomNameAndBranch(String roomName, Long branchId) {

        return roomRepository
                .existsByRoomNameAndBranch_BranchId(roomName, branchId);
    }

    public List<Room> searchRooms(String keyword, Long branchId) {

        if (keyword != null && !keyword.isEmpty() && branchId != null) {
            return roomRepository
                    .findByRoomNameContainingIgnoreCaseAndBranch_BranchId(keyword, branchId);
        }

        if (keyword != null && !keyword.isEmpty()) {
            return roomRepository
                    .findByRoomNameContainingIgnoreCase(keyword);
        }

        if (branchId != null) {
            return roomRepository
                    .findByBranchBranchId(branchId);
        }

        return roomRepository.findAll();
    }

    public Page<Room> searchRoomsPaging(String keyword, Long branchId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (keyword != null && !keyword.isEmpty() && branchId != null) {
            return roomRepository.findByRoomNameContainingIgnoreCaseAndBranchBranchId(
                    keyword, branchId, pageable);
        }

        if (keyword != null && !keyword.isEmpty()) {
            return roomRepository.findByRoomNameContainingIgnoreCase(keyword, pageable);
        }

        if (branchId != null) {
            return roomRepository.findByBranchBranchId(branchId, pageable);
        }

        return roomRepository.findAll(pageable);
    }

    public Room getById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));
    }

}
