package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.StaffDTO;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<StaffDTO> findAllStaff() {
        // 1. Chỉ định các vai trò thuộc nhóm nhân viên quản lý
        List<User.UserRole> staffRoles = Arrays.asList(
                User.UserRole.ADMIN,
                User.UserRole.MANAGER,
                User.UserRole.STAFF
        );

        List<User> users = userRepository.findByRoleIn(staffRoles);

        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private StaffDTO convertToDTO(User user) {
        StaffDTO dto = new StaffDTO();

        dto.setUser_id(user.getUserId());
        dto.setFull_name(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());

        dto.setRole(user.getRole() != null ? user.getRole().name() : "");
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : "inactive");

        if (user.getBranch() != null) {
            dto.setBranch_id(user.getBranch().getBranchId());
            dto.setBranch_name(user.getBranch().getBranchName());
        } else {
            dto.setBranch_name("Toàn hệ thống");
        }

        if (user.getCreatedAt() != null) {
            dto.setCreated_at(user.getCreatedAt().toString());
        }

        return dto;
    }
}