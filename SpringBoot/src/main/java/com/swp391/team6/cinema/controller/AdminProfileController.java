package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/admin/profile", "/manager/profile"})
public class AdminProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private CinemaBranchRepository cinemaBranchRepository;

    @GetMapping
    public String viewProfile(HttpSession session,
                              Model model,
                              RedirectAttributes ra,
                              HttpServletRequest request) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/auth/login";
        }
        if (!isAuthorized(sessionUser, request)) {
            ra.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }

        User currentUser = userService.getUserById(sessionUser.getUserId());
        model.addAttribute("user", currentUser);
        model.addAttribute("branchName", resolveBranchName(currentUser));
        model.addAttribute("profileBasePath", resolveBasePath(request));
        return "admin-profile";
    }

    @PostMapping("/update-info")
    public String updateProfileInfo(@RequestParam String fullName,
                                    @RequestParam String phone,
                                    HttpSession session,
                                    RedirectAttributes ra,
                                    HttpServletRequest request) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null || !isAuthorized(sessionUser, request)) {
            return "redirect:/auth/login";
        }

        try {
            userService.updateBasicInfo(sessionUser.getUserId(), fullName, phone);

            sessionUser.setFullName(fullName);
            sessionUser.setPhone(phone);
            session.setAttribute("loggedInUser", sessionUser);

            ra.addFlashAttribute("success", "Cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:" + resolveBasePath(request);
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes ra,
                                 HttpServletRequest request) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null || !isAuthorized(sessionUser, request)) {
            return "redirect:/auth/login";
        }

        String basePath = resolveBasePath(request);

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu xác nhận không trùng khớp!");
            return "redirect:" + basePath;
        }

        try {
            boolean success = userService.changeStaffPassword(sessionUser.getUserId(), oldPassword, newPassword);
            if (success) {
                ra.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            } else {
                ra.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác!");
            }
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:" + basePath;
    }

    private boolean isAuthorized(User user, HttpServletRequest request) {
        String basePath = resolveBasePath(request);
        if ("/admin/profile".equals(basePath)) {
            return user.getRole() == User.UserRole.ADMIN;
        }
        return user.getRole() == User.UserRole.MANAGER || user.getRole() == User.UserRole.ADMIN;
    }

    private String resolveBasePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin/profile")) {
            return "/admin/profile";
        }
        if (uri.startsWith("/manager/profile")) {
            return "/manager/profile";
        }
        return "/admin/profile";
    }

    private String resolveBranchName(User user) {
        if (user == null) {
            return "Toàn hệ thống";
        }
        Long branchId = user.getBranchId();
        if (branchId == null) {
            return "Toàn hệ thống";
        }
        return cinemaBranchRepository.findById(branchId)
                .map(b -> b.getBranchName() != null ? b.getBranchName() : "Chi nhánh #" + branchId)
                .orElse("Chi nhánh #" + branchId);
    }
}
