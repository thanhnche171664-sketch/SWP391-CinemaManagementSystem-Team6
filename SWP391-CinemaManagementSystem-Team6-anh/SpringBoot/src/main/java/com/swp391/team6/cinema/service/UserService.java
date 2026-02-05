package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.CustomerDTO;
import com.swp391.team6.cinema.dto.StaffDTO;
import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CinemaBranchRepository branchRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<StaffDTO> findAllStaff() {
        List<User.UserRole> staffRoles = Arrays.asList(
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

        dto.setCreated_at(user.getCreatedAt());

        return dto;
    }

    public void saveStaff(StaffDTO dto) {
        User user;
        if (dto.getUser_id() != null) {
            user = userRepository.findById(dto.getUser_id()).orElse(new User());
        } else {
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
        customer.setStatus(User.UserStatus.valueOf(dto.getStatus()));
        userRepository.save(customer);
    }
    
    /**
     * Xác thực đăng nhập
     * @return Map chứa: success (boolean), message (String), user (User nếu thành công)
     */
    public Map<String, Object> authenticateUser(String email, String password) {
        Map<String, Object> result = new HashMap<>();
        
        // Tìm user theo email
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            result.put("success", false);
            result.put("message", "Email không tồn tại!");
            return result;
        }
        
        User user = userOptional.get();
        
        // Kiểm tra password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            result.put("success", false);
            result.put("message", "Mật khẩu không chính xác!");
            return result;
        }
        
        // Kiểm tra tài khoản chưa verify
        if (user.getIsVerify() == null || !user.getIsVerify()) {
            result.put("success", false);
            result.put("message", "Tài khoản chưa được xác thực! Vui lòng kiểm tra email để verify.");
            return result;
        }
        
        // Kiểm tra tài khoản bị khóa
        if (user.getStatus() == User.UserStatus.inactive) {
            result.put("success", false);
            result.put("message", "Tài khoản đã bị khóa bởi Admin. Vui lòng liên hệ hỗ trợ.");
            return result;
        }
        
        // Đăng nhập thành công
        result.put("success", true);
        result.put("message", "Đăng nhập thành công!");
        result.put("user", user);
        
        return result;
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}