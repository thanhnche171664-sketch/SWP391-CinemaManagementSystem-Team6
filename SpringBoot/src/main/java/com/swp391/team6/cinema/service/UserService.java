package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.ChangePasswordDTO;
import com.swp391.team6.cinema.dto.CustomerDTO;
import com.swp391.team6.cinema.dto.StaffDTO;
import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
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

    @Autowired
    private CinemaBranchRepository branchRepository;

    public List<StaffDTO> findAllStaff() {
        List<User.UserRole> staffRoles = Arrays.asList(
                User.UserRole.MANAGER,
                User.UserRole.STAFF
        );
        return userRepository.findByRoleIn(staffRoles).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void updateProfile(User updatedUser){
            User user = userRepository.findById(updatedUser.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(updatedUser.getFullName());
        user.setEmail(updatedUser.getEmail());
        user.setPhone(updatedUser.getPhone());

        userRepository.save(user);
        }

    public void changePassword(Long userId, ChangePasswordDTO dto) {
        // 1. Tìm user trong database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // 2. Kiểm tra mật khẩu cũ (So sánh trực tiếp vì hiện tại bạn chưa dùng BCrypt)
        if (!user.getPasswordHash().equals(dto.getOldPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }

        if (dto.getNewPassword().length() < 8) {
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 8 ký tự!");
        }

        // 3. Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp nhau không
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Xác nhận mật khẩu mới không khớp!");
        }

        // 4. Cập nhật và lưu
        user.setPasswordHash(dto.getNewPassword());
        userRepository.save(user);
    }

    public void saveStaff(StaffDTO dto) {
        User user;
        if (dto.getUser_id() != null) {
            user = userRepository.findById(dto.getUser_id())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            if (!user.getRole().equals(User.UserRole.ADMIN) && "ADMIN".equals(dto.getRole())) {
                throw new RuntimeException("Không được phép nâng cấp lên quyền ADMIN");
            }
        } else {
            if ("ADMIN".equals(dto.getRole())) {
                throw new RuntimeException("Không được phép tạo tài khoản ADMIN mới");
            }
            user = new User();
            user.setPasswordHash("123456");
        }

        user.setFullName(dto.getFull_name());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(User.UserRole.valueOf(dto.getRole()));
        user.setBranchId(dto.getBranch_id());
        user.setStatus(User.UserStatus.valueOf(dto.getStatus()));

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPasswordHash(dto.getPassword());
        }

        userRepository.save(user);
    }

    public void toggleStatus(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus(user.getStatus() == User.UserStatus.active ?
                    User.UserStatus.inactive : User.UserStatus.active);
            userRepository.save(user);
        });
    }

    public void deleteStaff(Long id) {
        userRepository.deleteById(id);
    }

    public List<CinemaBranch> findAllBranches() {
        return branchRepository.findAll();
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
        dto.setCreated_at(user.getCreatedAt());
        return dto;
    }

    public List<CustomerDTO> findAllCustomers() {
        List<User.UserRole> roles = Arrays.asList(User.UserRole.CUSTOMER);
        return userRepository.findByRoleIn(roles).stream()
                .map(this::convertToCustomerDTO)
                .collect(Collectors.toList());
    }

    private CustomerDTO convertToCustomerDTO(User user) {
        CustomerDTO dto = new CustomerDTO();
        dto.setUser_id(user.getUserId());
        dto.setFull_name(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus().name());
        dto.setCreated_at(user.getCreatedAt());
        return dto;
    }

    public void updateCustomer(CustomerDTO dto) {
        User customer = userRepository.findById(dto.getUser_id())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        customer.setFullName(dto.getFull_name());
        customer.setPhone(dto.getPhone());
        customer.setStatus(User.UserStatus.valueOf(dto.getStatus()));
        userRepository.save(customer);
    }
}
