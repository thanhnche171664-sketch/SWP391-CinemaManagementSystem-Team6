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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/news")
@RequiredArgsConstructor
public class ManagerNewsController {

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
        User user = requireManager(session, redirectAttributes);
        if (user == null) return "redirect:/auth/login";

        Page<News> newsPage = newsService.getNewsForManagement(user, keyword, type, status, sort, page, size);

        model.addAttribute("newsList", newsPage.getContent());
        model.addAttribute("currentPage", newsPage.getNumber());
        model.addAttribute("totalPages", newsPage.getTotalPages());
        model.addAttribute("size", newsPage.getSize());
        model.addAttribute("keyword", keyword);
        model.addAttribute("typeFilter", type);
        model.addAttribute("statusFilter", status);
        model.addAttribute("sortFilter", sort);
        model.addAttribute("newsTypes", News.NewsType.values());
        model.addAttribute("newsStatuses", News.NewsStatus.values());
        model.addAttribute("newsBasePath", "/manager/news");
        model.addAttribute("user", user);
        return "news-management";
    }

    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) return "redirect:/auth/login";

        News news = new News();
        news.setBranchId(user.getBranchId());

        model.addAttribute("news", news);
        model.addAttribute("isEdit", false);
        model.addAttribute("newsTypes", News.NewsType.values());
        model.addAttribute("newsStatuses", News.NewsStatus.values());
        model.addAttribute("isAdmin", false);
        model.addAttribute("branches", cinemaBranchRepository.findAll());
        model.addAttribute("newsBasePath", "/manager/news");
        model.addAttribute("user", user);
        return "news-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) return "redirect:/auth/login";

        try {
            News news = newsService.getNewsForEdit(id, user);
            model.addAttribute("news", news);
            model.addAttribute("isEdit", true);
            model.addAttribute("newsTypes", News.NewsType.values());
            model.addAttribute("newsStatuses", News.NewsStatus.values());
            model.addAttribute("isAdmin", false);
            model.addAttribute("branches", cinemaBranchRepository.findAll());
            model.addAttribute("newsBasePath", "/manager/news");
            model.addAttribute("user", user);
            return "news-form";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/manager/news";
        }
    }

    @PostMapping("/save")
    public String saveNews(@ModelAttribute News news,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) return "redirect:/auth/login";

        try {
            if (news.getNewsId() == null) {
                newsService.createNews(news, user);
                redirectAttributes.addFlashAttribute("success", "Tạo tin tức thành công.");
            } else {
                newsService.updateNews(news.getNewsId(), news, user);
                redirectAttributes.addFlashAttribute("success", "Cập nhật tin tức thành công.");
            }
            return "redirect:/manager/news";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (news.getNewsId() == null) {
                return "redirect:/manager/news/create";
            }
            return "redirect:/manager/news/edit/" + news.getNewsId();
        }
    }

    @GetMapping("/hide/{id}")
    public String hideNews(@PathVariable Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) return "redirect:/auth/login";

        try {
            newsService.hideNews(id, user);
            redirectAttributes.addFlashAttribute("success", "Đã ẩn tin tức.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/news";
    }

    private User requireManager(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.MANAGER) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập.");
            return null;
        }
        if (user.getBranchId() == null) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản quản lý chưa có chi nhánh.");
            return null;
        }
        return user;
    }
}
