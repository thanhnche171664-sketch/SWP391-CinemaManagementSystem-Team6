package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.News;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.service.NewsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/news")
@RequiredArgsConstructor
public class NewsManagementController {

    private final NewsService newsService;
    private final CinemaBranchRepository cinemaBranchRepository;

    @GetMapping
    public String viewNews(HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) News.NewsType type,
                           @RequestParam(required = false) News.NewsStatus status,
                           @RequestParam(defaultValue = "latest") String sort) {
        User user = requireAdminOrManager(session, redirectAttributes);
        if (user == null) return "redirect:/auth/login";

        Page<News> newsPage = newsService.getNewsForManagement(user, keyword, type, status, sort, page, size);

        model.addAttribute("newsList", newsPage.getContent());
        model.addAttribute("currentPage", newsPage.getNumber());
        model.addAttribute("totalPages", newsPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("typeFilter", type);
        model.addAttribute("statusFilter", status);
        model.addAttribute("sortFilter", sort);
        model.addAttribute("newsTypes", News.NewsType.values());
        model.addAttribute("newsStatuses", News.NewsStatus.values());
        model.addAttribute("user", user);
        return "news-management";
    }

    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = requireAdminOrManager(session, redirectAttributes);
        if (user == null) return "redirect:/auth/login";

        News news = new News();
        if (user.getRole() == User.UserRole.MANAGER) {
            news.setBranchId(user.getBranchId());
        }

        model.addAttribute("news", news);
        model.addAttribute("isEdit", false);
        model.addAttribute("newsTypes", News.NewsType.values());
        model.addAttribute("newsStatuses", News.NewsStatus.values());
        model.addAttribute("isAdmin", user.getRole() == User.UserRole.ADMIN);
        model.addAttribute("branches", cinemaBranchRepository.findAll());
        model.addAttribute("user", user);
        return "news-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User user = requireAdminOrManager(session, redirectAttributes);
        if (user == null) return "redirect:/auth/login";

        try {
            News news = newsService.getNewsForEdit(id, user);
            model.addAttribute("news", news);
            model.addAttribute("isEdit", true);
            model.addAttribute("newsTypes", News.NewsType.values());
            model.addAttribute("newsStatuses", News.NewsStatus.values());
            model.addAttribute("isAdmin", user.getRole() == User.UserRole.ADMIN);
            model.addAttribute("branches", cinemaBranchRepository.findAll());
            model.addAttribute("user", user);
            return "news-form";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/news";
        }
    }

    @PostMapping("/save")
    public String saveNews(@ModelAttribute News news,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User user = requireAdminOrManager(session, redirectAttributes);
        if (user == null) return "redirect:/auth/login";

        try {
            if (news.getNewsId() == null) {
                newsService.createNews(news, user);
                redirectAttributes.addFlashAttribute("success", "Tạo tin tức thành công.");
            } else {
                newsService.updateNews(news.getNewsId(), news, user);
                redirectAttributes.addFlashAttribute("success", "Cập nhật tin tức thành công.");
            }
            return "redirect:/admin/news";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (news.getNewsId() == null) {
                return "redirect:/admin/news/create";
            }
            return "redirect:/admin/news/edit/" + news.getNewsId();
        }
    }

    @GetMapping("/hide/{id}")
    public String hideNews(@PathVariable Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User user = requireAdminOrManager(session, redirectAttributes);
        if (user == null) return "redirect:/auth/login";

        try {
            newsService.hideNews(id, user);
            redirectAttributes.addFlashAttribute("success", "Đã ẩn tin tức.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/news";
    }

    private User requireAdminOrManager(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || (user.getRole() != User.UserRole.ADMIN && user.getRole() != User.UserRole.MANAGER)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập.");
            return null;
        }
        return user;
    }
}
