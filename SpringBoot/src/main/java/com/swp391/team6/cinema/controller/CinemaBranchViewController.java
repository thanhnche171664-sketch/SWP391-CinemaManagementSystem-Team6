package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.service.CinemaBranchService;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
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
    public String showBranches(Model model) {
        model.addAttribute("branches", cinemaBranchService.getAllBranches());
        return "branch-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("branch", new CinemaBranch());
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

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("branch", cinemaBranchService.getBranchById(id));
        return "branch-edit";
    }

    @PostMapping("/save")
    public String saveBranch(@ModelAttribute CinemaBranch branch) {
        cinemaBranchService.save(branch);
        return "redirect:/branches";
    }

    @GetMapping("/delete/{id}")
    public String deleteBranch(@PathVariable Long id) {
        cinemaBranchService.deleteById(id);
        return "redirect:/branches";
    }

    @PostMapping("/update/{id}")
    public String updateBranch(@PathVariable Long id,
                               @ModelAttribute CinemaBranch branch) {
        cinemaBranchService.updateBranch(id, branch);
        return "redirect:/branches";
    }

    @GetMapping("/search")
    public String searchBranches(@RequestParam("keyword") String keyword,
                                 Model model) {

        model.addAttribute("branches",
                cinemaBranchService.searchBranches(keyword));

        model.addAttribute("keyword", keyword); // giữ lại text search
        return "branch-list";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleBranchStatus(@PathVariable Long id) {
        cinemaBranchService.toggleStatus(id);
        return "redirect:/branches";
    }

}