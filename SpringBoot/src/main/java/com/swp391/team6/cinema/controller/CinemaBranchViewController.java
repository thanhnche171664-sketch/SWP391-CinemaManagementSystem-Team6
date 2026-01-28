package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.service.CinemaBranchService;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String createBranch(@ModelAttribute CinemaBranch branch) {
        cinemaBranchService.createBranch(branch);
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

}
