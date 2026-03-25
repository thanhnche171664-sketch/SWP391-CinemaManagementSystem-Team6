package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Pricing;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.service.PricingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/manager/pricing")
@RequiredArgsConstructor
public class ManagerPricingController {

    private final PricingService pricingService;
    private final CinemaBranchRepository branchRepository;

    @GetMapping
    public String pricingPage(HttpSession session,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Long branchId = user.getBranchId();
        List<Pricing> pricings = pricingService.getPricingByBranch(branchId);

        BigDecimal normalPrice = BigDecimal.ZERO;
        BigDecimal vipPrice = BigDecimal.ZERO;
        BigDecimal couplePrice = BigDecimal.ZERO;

        for (Pricing p : pricings) {
            if (p.getSeatType() == Seat.SeatType.NORMAL) {
                normalPrice = p.getPrice();
            }
            if (p.getSeatType() == Seat.SeatType.VIP) {
                vipPrice = p.getPrice();
            }
            if (p.getSeatType() == Seat.SeatType.COUPLE) {
                couplePrice = p.getPrice();
            }
        }

        model.addAttribute("normalPrice", normalPrice);
        model.addAttribute("vipPrice", vipPrice);
        model.addAttribute("couplePrice", couplePrice);
        model.addAttribute("branches", branchRepository.findAll());
        model.addAttribute("branchId", branchId);
        model.addAttribute("branchName", branchRepository.findById(branchId)
                .map(b -> b.getBranchName() != null ? b.getBranchName() : "Chi nhánh #" + branchId)
                .orElse("Chi nhánh #" + branchId));
        model.addAttribute("pricings", pricings);
        model.addAttribute("pricingBasePath", "/manager/pricing");
        model.addAttribute("roomBasePath", "/manager/rooms");
        model.addAttribute("branchBasePath", "/branches");
        model.addAttribute("isManager", true);

        return "pricing-config";
    }

    @PostMapping("/update")
    public String updatePricing(@RequestParam Long branchId,
                                @RequestParam("pricings[0].seatType") Seat.SeatType normalType,
                                @RequestParam("pricings[0].price") BigDecimal normalPrice,
                                @RequestParam("pricings[1].seatType") Seat.SeatType vipType,
                                @RequestParam("pricings[1].price") BigDecimal vipPrice,
                                @RequestParam("pricings[2].seatType") Seat.SeatType coupleType,
                                @RequestParam("pricings[2].price") BigDecimal couplePrice,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        if (!user.getBranchId().equals(branchId)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền cập nhật cấu hình ghế này.");
            return "redirect:/manager/pricing";
        }

        pricingService.updatePrice(branchId, normalType, normalPrice);
        pricingService.updatePrice(branchId, vipType, vipPrice);
        pricingService.updatePrice(branchId, coupleType, couplePrice);

        return "redirect:/manager/pricing";
    }

    private User requireManager(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.MANAGER) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return null;
        }
        if (user.getBranchId() == null) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản quản lý chưa có chi nhánh.");
            return null;
        }
        return user;
    }
}
