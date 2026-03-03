package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.StaffDTO;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/staff")
public class StaffController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String showStaffPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || (user.getRole() != User.UserRole.ADMIN && user.getRole() != User.UserRole.MANAGER)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }
        
        model.addAttribute("staffList", userService.findAllStaff());
        model.addAttribute("branches", userService.findAllBranches());
        model.addAttribute("newStaff", new StaffDTO());
        model.addAttribute("user", user);
        return "staff-management";
    }

    @PostMapping("/save")
    public String saveStaff(@ModelAttribute("newStaff") StaffDTO staffDTO) {
        userService.saveStaff(staffDTO);
        return "redirect:/admin/staff";
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id) {
        userService.toggleStatus(id);
        return "redirect:/admin/staff";
    }

    @GetMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Long id) {
        userService.deleteStaff(id);
        return "redirect:/admin/staff";
    }


}