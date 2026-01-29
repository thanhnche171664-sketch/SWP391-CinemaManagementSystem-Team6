package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.StaffDTO;
import com.swp391.team6.cinema.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/staff")
public class StaffController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String showStaffPage(Model model) {
        model.addAttribute("staffList", userService.findAllStaff());
        model.addAttribute("branches", userService.findAllBranches());
        model.addAttribute("newStaff", new StaffDTO());
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