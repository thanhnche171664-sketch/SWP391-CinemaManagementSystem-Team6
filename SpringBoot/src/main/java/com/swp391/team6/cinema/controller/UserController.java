package com.swp391.team6.cinema.controller;
import com.swp391.team6.cinema.dto.ChangePasswordDTO;
import com.swp391.team6.cinema.service.UserService; // Thay đổi tùy theo package thực tế
import com.swp391.team6.cinema.entity.User;        // Thay đổi tùy theo package thực tế
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String viewProfile(
            @RequestParam(value = "edit", defaultValue = "false") boolean edit,
            Model model,
            HttpSession session) {

        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/auth/login?redirect=/profile";
        }

        User user = userService.getUserById(sessionUser.getUserId());
        if (user == null) {
            return "redirect:/auth/login?redirect=/profile";
        }
        model.addAttribute("user", user);
        model.addAttribute("edit", edit);

        return "profile";
    }


    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User user,
                                RedirectAttributes ra,
                                Model model,
                                HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/auth/login?redirect=/profile";
        }

        user.setUserId(sessionUser.getUserId());
        try {
            userService.updateProfile(user);
            sessionUser.setFullName(user.getFullName());
            sessionUser.setEmail(user.getEmail());
            sessionUser.setPhone(user.getPhone());
            session.setAttribute("loggedInUser", sessionUser);
            ra.addFlashAttribute("message", "Cập nhật thông tin cá nhân thành công!");
            return "redirect:/profile";
        } catch (RuntimeException e) {
            model.addAttribute("user", user);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("edit", true);
            return "profile";
        }
    }

    // 1. Hiển thị trang đổi mật khẩu
    @GetMapping("/profile/change-password")
    public String viewChangePassword(Model model) {
        model.addAttribute("passwordDTO", new ChangePasswordDTO());
        return "change-password";
    }

    // 2. Xử lý dữ liệu gửi lên từ Form
    @PostMapping("/profile/change-password")
    public String handleChangePassword(
            @Valid @ModelAttribute("passwordDTO") ChangePasswordDTO dto,
            BindingResult result, // Đối tượng chứa kết quả kiểm tra lỗi
            RedirectAttributes ra,
            Model model,
            HttpSession session) {

        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/auth/login?redirect=/profile";
        }

        // 1. Kiểm tra lỗi định dạng (độ dài, ký tự...) từ các Annotation trong DTO
        if (result.hasErrors()) {
            return "change-password";
        }

        // 2. Kiểm tra logic: Mật khẩu mới và xác nhận phải khớp nhau
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            // Thêm lỗi thủ công vào trường confirmPassword
            result.rejectValue("confirmPassword", "error.passwordDTO", "Mật khẩu xác nhận không khớp!");
            return "change-password";
        }

        try {
            Long currentUserId = sessionUser.getUserId();
            userService.changePassword(currentUserId, dto);

            ra.addFlashAttribute("message", "Đổi mật khẩu thành công!");
            return "redirect:/profile";
        } catch (RuntimeException e) {
            // Lỗi từ Service (ví dụ: mật khẩu cũ không đúng)
            model.addAttribute("error", e.getMessage());
            return "change-password";
        }
    }

}

