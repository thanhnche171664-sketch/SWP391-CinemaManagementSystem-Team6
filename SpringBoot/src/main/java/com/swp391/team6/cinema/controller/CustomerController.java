package com.swp391.team6.cinema.controller;


import com.swp391.team6.cinema.dto.CustomerDTO;
import com.swp391.team6.cinema.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/customers")
public class CustomerController {
    @Autowired
    private UserService userService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customerList", userService.findAllCustomers());
        model.addAttribute("editedCustomer", new CustomerDTO());
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