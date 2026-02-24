package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/auth/login";
        }

        // Refresh from DB to get latest data
        User freshUser = userService.getUserByEmail(user.getEmail());
        model.addAttribute("user", freshUser);
        model.addAttribute("edit", edit);

        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User user,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/auth/login";
        }

        try {
            userService.updateProfile(user);
            // Update session with new data
            User updatedUser = userService.getUserByEmail(user.getEmail());
            session.setAttribute("loggedInUser", updatedUser);
        } catch (RuntimeException e) {
            model.addAttribute("user", user);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("edit", true);
            return "profile";
        }
        return "redirect:/profile";
    }
}

