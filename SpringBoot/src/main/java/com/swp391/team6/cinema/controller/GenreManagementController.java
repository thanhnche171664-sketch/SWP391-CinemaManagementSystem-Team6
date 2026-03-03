package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.GenreDTO;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.GenreService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/genres")
@RequiredArgsConstructor
public class GenreManagementController {

    private final GenreService genreService;

    @GetMapping
    public String list(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (!isAdmin(user)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }

        model.addAttribute("genreList", genreService.getAllGenres());
        model.addAttribute("newGenre", new GenreDTO());
        model.addAttribute("user", user);
        return "genre-management";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("newGenre") GenreDTO genreDTO,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (!isAdmin(user)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }

        try {
            genreService.saveGenre(genreDTO);
            redirectAttributes.addFlashAttribute("success", "Lưu thể loại thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể lưu thể loại.");
        }
        return "redirect:/admin/genres";
    }

    @GetMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (!isAdmin(user)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }

        try {
            genreService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật trạng thái.");
        }
        return "redirect:/admin/genres";
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == User.UserRole.ADMIN;
    }
}
