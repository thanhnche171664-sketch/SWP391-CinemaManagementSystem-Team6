package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.ChangePasswordDTO;
import com.swp391.team6.cinema.dto.BookingDTO;
import com.swp391.team6.cinema.dto.CustomerDTO;
import com.swp391.team6.cinema.dto.StaffDTO;
import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.BookingRepository;
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

    @Autowired
    private BookingRepository bookingRepository;


    public List<StaffDTO> findAllStaff() {
        List<User.UserRole> staffRoles = Arrays.asList(
                User.UserRole.MANAGER,
                User.UserRole.STAFF
        );

        return userRepository.findByRoleIn(staffRoles)
                .stream()
                .map(this::convertToStaffDTO)
                .collect(Collectors.toList());
    }

    private StaffDTO convertToStaffDTO(User user) {
        StaffDTO dto = new StaffDTO();
        dto.setUser_id(user.getUserId());
        dto.setFull_name(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        dto.setStatus(user.getStatus().name());

        if (user.getBranch() != null) {
            dto.setBranch_id(user.getBranch().getBranchId());
            dto.setBranch_name(user.getBranch().getBranchName());
        } else {
            dto.setBranch_name("Toàn hệ thống");
        }
        return dto;
    }

    public void saveStaff(StaffDTO dto) {
        User user = (dto.getUser_id() != null)
                ? userRepository.findById(dto.getUser_id()).orElse(new User())
                : new User();

        if (user.getUserId() == null) {
            user.setPasswordHash(passwordEncoder.encode("123456"));
        }

        user.setFullName(dto.getFull_name());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(User.UserRole.valueOf(dto.getRole()));
        user.setStatus(User.UserStatus.valueOf(dto.getStatus()));
        user.setIsVerify(true);

        if (dto.getBranch_id() != null) {
            CinemaBranch branch = branchRepository.findById(dto.getBranch_id()).orElse(null);
            user.setBranch(branch);
        } else {
            user.setBranch(null);
        }

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
    }

    public void toggleStatus(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus(user.getStatus() == User.UserStatus.active
                    ? User.UserStatus.inactive
                    : User.UserStatus.active);
            userRepository.save(user);
        });
    }

    public void deleteStaff(Long id) {
        userRepository.deleteById(id);
    }

    public List<CustomerDTO> findAllCustomers() {
        return userRepository.findByRoleIn(List.of(User.UserRole.CUSTOMER))
                .stream()
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
        User user = userRepository.findById(dto.getUser_id())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        user.setStatus(User.UserStatus.valueOf(dto.getStatus()));
        userRepository.save(user);
    }

    public void updateProfile(User user) {
        User existingUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng để cập nhật"));
        existingUser.setFullName(user.getFullName());
        existingUser.setPhone(user.getPhone());
        userRepository.save(existingUser);
    }

    public void changePassword(String email, ChangePasswordDTO dto) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new Exception("Mật khẩu cũ không chính xác");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new Exception("Mật khẩu mới không khớp");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    public List<BookingDTO> findBookingsByCustomerId(Long userId) {
        return bookingRepository.findByUserUserId(userId)
                .stream()
                .map(this::convertToBookingDTO)
                .collect(Collectors.toList());
    }

    private BookingDTO convertToBookingDTO(Booking b) {
        BookingDTO dto = new BookingDTO();
        dto.setBooking_id(b.getBookingId());
        dto.setTotal_amount(b.getTotalAmount());
        dto.setStatus(b.getStatus().name());
        dto.setBooking_time(b.getBookingTime());

        if (b.getShowtime() != null && b.getShowtime().getMovie() != null) {
            dto.setMovie_title(b.getShowtime().getMovie().getTitle());
        }

        if (b.getShowtime() != null && b.getShowtime().getRoom() != null) {
            dto.setBranch_name(b.getShowtime().getRoom().getBranch().getBranchName());
        }

        dto.setSeat_names(
                b.getBookingSeats().stream()
                        .map(bs -> bs.getSeat().getSeatNumber())
                        .collect(Collectors.joining(", "))
        );

        return dto;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}