package com.swp391.team6.cinema.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
        if (dto.getEmail() == null || !dto.getEmail().contains("@")) {
            throw new RuntimeException("Email không hợp lệ!");
        }

        if (dto.getPhone() == null || !dto.getPhone().matches("\\d{10}")) {
            throw new RuntimeException("Số điện thoại phải bao gồm đúng 10 chữ số!");
        }

        Optional<User> userByEmail = userRepository.findByEmail(dto.getEmail());
        if (userByEmail.isPresent() && !userByEmail.get().getUserId().equals(dto.getUser_id())) {
            throw new RuntimeException("Email này đã được sử dụng bởi một tài khoản khác!");
        }

        User user;
        if (dto.getUser_id() != null) {
            user = userRepository.findById(dto.getUser_id())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        } else {
            user = new User();
            String rawPassword = (dto.getPassword() == null || dto.getPassword().isEmpty()) ? "123456" : dto.getPassword();
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setIsVerify(true);
        }

        user.setFullName(dto.getFull_name());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        User.UserRole role = User.UserRole.valueOf(dto.getRole());
        user.setRole(role);
        user.setBranchId(dto.getBranch_id());
        user.setStatus(User.UserStatus.valueOf(dto.getStatus()));
        if (role != User.UserRole.CUSTOMER) {
            user.setIsVerify(true);
        }

        if (dto.getUser_id() != null && dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
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
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus(User.UserStatus.inactive);
            userRepository.save(user);
        });
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

    public Page<CustomerDTO> findCustomersPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<User.UserRole> customerRoles = Collections.singletonList(User.UserRole.CUSTOMER);

        return userRepository.findByRoleIn(customerRoles, pageable)
                .map(this::convertToCustomerDTO);
    }

    public Page<StaffDTO> findStaffPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("userId").descending());
        List<User.UserRole> staffRoles = Arrays.asList(User.UserRole.MANAGER, User.UserRole.STAFF);

        return userRepository.findByRoleIn(staffRoles, pageable)
                .map(this::convertToDTO);
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

    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (passwordEncoder.matches(oldPassword, user.getPasswordHash())) {

                user.setPasswordHash(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void updateProfile(User updatedUser){
        User user = userRepository.findById(updatedUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    @Autowired
    private BookingRepository bookingRepository;

    public List<BookingDTO> findBookingsByCustomerId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserUserId(userId);

        return bookings.stream().map(b -> {
            BookingDTO dto = new BookingDTO();
            dto.setBooking_id(b.getBookingId());

            if (b.getShowtime() != null) {

                if (b.getShowtime().getMovie() != null) {
                    dto.setMovie_title(b.getShowtime().getMovie().getTitle());
                } else {
                    dto.setMovie_title("N/A");
                }

                if (b.getShowtime().getRoom() != null && b.getShowtime().getRoom().getBranch() != null) {
                    dto.setBranch_name(b.getShowtime().getRoom().getBranch().getBranchName());
                } else {
                    dto.setBranch_name("N/A");
                }
            }

            if (b.getBookingSeats() != null && !b.getBookingSeats().isEmpty()) {
                String seats = b.getBookingSeats().stream()
                        .map(bs -> (bs.getSeat() != null) ? String.valueOf(bs.getSeat().getSeatNumber()) : "")
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining(", "));
                dto.setSeat_names(seats);
            } else {
                dto.setSeat_names("N/A");
            }

            dto.setTotal_amount(b.getTotalAmount());
            dto.setStatus(b.getStatus() != null ? b.getStatus().name() : "pending");
            dto.setBooking_time(b.getBookingTime());

            return dto;
        }).collect(Collectors.toList());
    }


    public Map<String, Object> authenticateUser(String email, String password) {
        Map<String, Object> result = new HashMap<>();

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            result.put("success", false);
            result.put("message", "Email không tồn tại!");
            return result;
        }

        User user = userOptional.get();

        String rawPassword = password == null ? "" : password;
        String storedPassword = user.getPasswordHash();
        boolean passwordMatches = false;
        if (storedPassword != null) {
            passwordMatches = passwordEncoder.matches(rawPassword, storedPassword);
            if (!passwordMatches && storedPassword.equals(rawPassword)) {
                passwordMatches = true;
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
                if (user.getRole() != User.UserRole.CUSTOMER &&
                        (user.getIsVerify() == null || !user.getIsVerify())) {
                    user.setIsVerify(true);
                }
                userRepository.save(user);
            }
        }
        if (!passwordMatches) {
            result.put("success", false);
            result.put("message", "Mật khẩu không chính xác!");
            return result;
        }

        // Kiểm tra tài khoản chưa verify (chỉ áp dụng cho CUSTOMER)
        if (user.getRole() == User.UserRole.CUSTOMER &&
                (user.getIsVerify() == null || !user.getIsVerify())) {
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