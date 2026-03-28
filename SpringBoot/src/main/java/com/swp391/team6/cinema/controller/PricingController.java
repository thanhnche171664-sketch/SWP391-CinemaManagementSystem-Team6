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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;
    private final CinemaBranchRepository branchRepository;

    @GetMapping
    public String pricingPage(
            @RequestParam(required = false) Long branchId,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        var branches = branchRepository.findAll();

        if (branchId == null && !branches.isEmpty()) {
            branchId = branches.get(0).getBranchId();
        }

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

        model.addAttribute("branches", branches);
        model.addAttribute("branchId", branchId);
        model.addAttribute("pricings", pricings);
        model.addAttribute("pricingBasePath", "/admin/pricing");
        model.addAttribute("roomBasePath", "/rooms");
        model.addAttribute("branchBasePath", "/branches");
        model.addAttribute("isManager", false);

        return "pricing-config";
    }

    @PostMapping("/update")
    public String updatePricing(
            @RequestParam Long branchId,
            @RequestParam("pricings[0].seatType") Seat.SeatType normalType,
            @RequestParam("pricings[0].price") BigDecimal normalPrice,

            @RequestParam("pricings[1].seatType") Seat.SeatType vipType,
            @RequestParam("pricings[1].price") BigDecimal vipPrice,

            @RequestParam("pricings[2].seatType") Seat.SeatType coupleType,
            @RequestParam("pricings[2].price") BigDecimal couplePrice,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        String validationError = pricingService.validateSeatPriceOrder(normalPrice, vipPrice, couplePrice);
        if (validationError != null) {
            redirectAttributes.addFlashAttribute("error", validationError);
            return "redirect:/admin/pricing?branchId=" + branchId;
        }

        pricingService.updatePrice(branchId, normalType, normalPrice);
        pricingService.updatePrice(branchId, vipType, vipPrice);
        pricingService.updatePrice(branchId, coupleType, couplePrice);

        redirectAttributes.addFlashAttribute("success", "Lưu cấu hình ghế thành công.");
        return "redirect:/admin/pricing?branchId=" + branchId;
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
