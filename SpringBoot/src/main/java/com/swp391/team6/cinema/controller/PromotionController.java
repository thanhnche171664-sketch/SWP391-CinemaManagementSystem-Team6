package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Promotion;
import com.swp391.team6.cinema.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/admin/promotions")
public class PromotionController {

    private static final Logger log = LoggerFactory.getLogger(PromotionController.class);

    @Autowired
    private PromotionService promotionService;

    @GetMapping("/view")
    public String viewPromotions(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        Page<Promotion> promotionPage = promotionService.getAllPromotions(page, size);

        model.addAttribute("totalPromotions", promotionService.countAll());
        model.addAttribute("activeCount", promotionService.countActive());
        model.addAttribute("upcomingCount", 1);
        model.addAttribute("totalDiscountedAmount", "0 đ");

        model.addAttribute("promotions", promotionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotionPage.getTotalPages());
        model.addAttribute("totalElements", promotionPage.getTotalElements());

        return "promotion-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        return "promotion-create";
    }

    @PostMapping("/create-action")
    public String createPromotion(@ModelAttribute Promotion promotion) {
        try {
            promotionService.create(promotion);
            return "redirect:/admin/promotions/view";
        } catch (Exception e) {
            log.error("Lỗi khi tạo khuyến mãi: ", e);
            return "promotion-create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        try {
            model.addAttribute("promotion", promotionService.findById(id));
            return "promotion-edit";
        } catch (Exception e) {
            return "redirect:/admin/promotions/view";
        }
    }

    @PostMapping("/update-action")
    public String updatePromotion(@ModelAttribute Promotion promotion) {
        try {
            promotionService.update(promotion);
            return "redirect:/admin/promotions/view";
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật khuyến mãi: ", e);
            return "promotion-edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Integer id) {
        promotionService.softDelete(id);
        return "redirect:/admin/promotions/view";
    }
}