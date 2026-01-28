package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.service.CinemaBranchService;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class CinemaBranchViewController {

    private final CinemaBranchService cinemaBranchService;

    @GetMapping("/branches")
    public String showBranches(Model model) {
        model.addAttribute("branches", cinemaBranchService.getAllBranches());
        return "branch-list";
    }

    @GetMapping("/branches/new")
    public String showCreateForm(Model model) {
        model.addAttribute("branch", new CinemaBranch());
        return "branch-create";
    }

    @PostMapping("/branches")
    public String createBranch(@ModelAttribute CinemaBranch branch) {
        cinemaBranchService.createBranch(branch);
        return "redirect:/branches";
    }

}
