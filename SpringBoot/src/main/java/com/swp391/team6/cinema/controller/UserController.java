package com.swp391.team6.cinema.controller;
import com.swp391.team6.cinema.service.UserService;
import com.swp391.team6.cinema.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

}