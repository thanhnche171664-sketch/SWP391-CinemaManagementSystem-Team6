package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Room;
import com.swp391.team6.cinema.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
