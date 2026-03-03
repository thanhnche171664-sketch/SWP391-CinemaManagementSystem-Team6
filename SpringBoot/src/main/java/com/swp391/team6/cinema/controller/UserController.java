package com.swp391.team6.cinema.controller;
import com.swp391.team6.cinema.dto.ChangePasswordDTO;
import com.swp391.team6.cinema.service.UserService; // Thay đổi tùy theo package thực tế
import com.swp391.team6.cinema.entity.User;        // Thay đổi tùy theo package thực tế
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String viewProfile(
            @RequestParam(value = "edit", defaultValue = "false") boolean edit,
            Model model) {

        String email = "admin@gmail.com";

        User user = userService.getUserByEmail(email);
        model.addAttribute("user", user);
        model.addAttribute("edit", edit);

        return "profile";
    }


    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User user,
                                Model model) {
        try {
            userService.updateProfile(user);
        } catch (RuntimeException e) {
            model.addAttribute("user", user);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("edit", true);
            return "profile";
        }
        return "redirect:/profile";
    }


    @PostMapping("/api/user/change-password")
    @ResponseBody // Quan trọng: Trả về text/json thay vì tìm file HTML
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDTO dto, Principal principal) {
        try {
            String email = (principal != null) ? principal.getName() : "admin@gmail.com";
            userService.changePassword(email, dto);
            return ResponseEntity.ok("Đổi mật khẩu thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}



