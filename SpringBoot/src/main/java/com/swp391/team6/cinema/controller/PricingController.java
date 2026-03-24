package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Pricing;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            Model model) {

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
            @RequestParam("pricings[2].price") BigDecimal couplePrice
    ) {

        pricingService.updatePrice(branchId, normalType, normalPrice);
        pricingService.updatePrice(branchId, vipType, vipPrice);
        pricingService.updatePrice(branchId, coupleType, couplePrice);

        return "redirect:/admin/pricing?branchId=" + branchId;
    }


}
