package com.swp391.team6.cinema.controller;
import com.swp391.team6.cinema.service.UserService; // Thay đổi tùy theo package thực tế
import com.swp391.team6.cinema.entity.User;        // Thay đổi tùy theo package thực tế
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String viewProfile(Model model) {

        // demo: giả lập user đang login
        String email = "admin@gmail.com";

        User user = userService.getUserByEmail(email);
        model.addAttribute("user", user);

        return "profile";
    }
}

