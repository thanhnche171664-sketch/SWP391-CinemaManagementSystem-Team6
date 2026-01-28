package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.service.CinemaBranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class CinemaBranchViewController {

    private final CinemaBranchService cinemaBranchService;

    @GetMapping("/branches")
    public String showBranches(Model model) {
        model.addAttribute("branches", cinemaBranchService.getAllBranches());
        return "branch-list";
    }
}
