package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/staff") // Đường dẫn vào trang quản lý
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String showStaffPage(Model model) {
        model.addAttribute("staffList", userService.findAllStaff());

        return "staff-management";
    }
}