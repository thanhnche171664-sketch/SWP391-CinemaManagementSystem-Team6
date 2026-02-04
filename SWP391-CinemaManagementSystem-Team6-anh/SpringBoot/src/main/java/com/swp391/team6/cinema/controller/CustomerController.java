package com.swp391.team6.cinema.controller;


import com.swp391.team6.cinema.dto.CustomerDTO;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/customers")
public class CustomerController {
    @Autowired
    private UserService userService;

    @GetMapping
    public String list(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }
        
        model.addAttribute("customerList", userService.findAllCustomers());
        model.addAttribute("editedCustomer", new CustomerDTO());
        model.addAttribute("user", user);
        return "customer-management";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("editedCustomer") CustomerDTO dto) {
        userService.updateCustomer(dto);
        return "redirect:/admin/customers";
    }

    @GetMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id) {
        userService.toggleStatus(id);
        return "redirect:/admin/customers";
    }
}