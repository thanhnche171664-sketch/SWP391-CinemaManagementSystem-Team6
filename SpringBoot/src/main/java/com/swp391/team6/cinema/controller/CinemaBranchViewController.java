package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.CinemaBranchService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/branches")
public class CinemaBranchViewController {

    private final CinemaBranchService cinemaBranchService;

    @GetMapping
    public String showBranches(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("branches", cinemaBranchService.getAllBranches());
        model.addAttribute("branchBasePath", "/branches");
        model.addAttribute("roomBasePath", "/rooms");
        model.addAttribute("pricingBasePath", "/admin/pricing");
        model.addAttribute("isManager", false);
        return "branch-list";
    }

    @GetMapping("/new")
    public String showCreateForm(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("branch", new CinemaBranch());
        model.addAttribute("branchBasePath", "/branches");
        model.addAttribute("roomBasePath", "/rooms");
        model.addAttribute("pricingBasePath", "/admin/pricing");
        model.addAttribute("isManager", false);
        return "branch-create";
    }

    @PostMapping
    public String createBranch(@ModelAttribute CinemaBranch branch,
                               RedirectAttributes ra) {

        try {

            if (branch.getBranchName() == null || branch.getBranchName().isBlank()) {
                throw new RuntimeException("Tên rạp không được để trống");
            }

            if (branch.getBranchName().length() < 3) {
                throw new RuntimeException("Tên rạp phải >= 3 ký tự");
            }

            if (branch.getAddress() == null || branch.getAddress().isBlank()) {
                throw new RuntimeException("Địa chỉ không được để trống");
            }

            boolean exists = cinemaBranchService.existsByName(branch.getBranchName());
            if (exists) {
                throw new RuntimeException("Rạp đã tồn tại");
            }

            cinemaBranchService.createBranch(branch);

            ra.addFlashAttribute("success", "Thêm rạp thành công");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/branches/new";
        }

        return "redirect:/branches";
    }

    public String createBranch(@ModelAttribute CinemaBranch branch,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        cinemaBranchService.createBranch(branch);
        return "redirect:/branches";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("branch", cinemaBranchService.getBranchById(id));
        model.addAttribute("branchBasePath", "/branches");
        model.addAttribute("roomBasePath", "/rooms");
        model.addAttribute("pricingBasePath", "/admin/pricing");
        model.addAttribute("isManager", false);
        return "branch-edit";
    }

    @PostMapping("/save")
    public String saveBranch(@ModelAttribute CinemaBranch branch,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        cinemaBranchService.save(branch);
        return "redirect:/branches";
    }

    @GetMapping("/delete/{id}")
    public String deleteBranch(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        cinemaBranchService.deleteById(id);
        return "redirect:/branches";
    }

    @PostMapping("/update/{id}")
    public String updateBranch(@PathVariable Long id,
                               @ModelAttribute CinemaBranch branch,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        cinemaBranchService.updateBranch(id, branch);
        return "redirect:/branches";
    }

    @GetMapping("/search")
    public String searchBranches(@RequestParam("keyword") String keyword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("branches",
                cinemaBranchService.searchBranches(keyword));

        model.addAttribute("keyword", keyword); // giữ lại text search
        model.addAttribute("branchBasePath", "/branches");
        model.addAttribute("roomBasePath", "/rooms");
        model.addAttribute("pricingBasePath", "/admin/pricing");
        model.addAttribute("isManager", false);
        return "branch-list";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleBranchStatus(@PathVariable Long id,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        cinemaBranchService.toggleStatus(id);
        return "redirect:/branches";
    }

    private User requireAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return null;
        }
        return user;
    }
}