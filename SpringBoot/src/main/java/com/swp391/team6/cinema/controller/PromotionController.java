package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Promotion;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.PromotionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/admin/promotions")
public class PromotionController {

    private static final Logger log = LoggerFactory.getLogger(PromotionController.class);

    @Autowired
    private PromotionService promotionService;

    @GetMapping("/view")
    public String viewPromotions(Model model,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String status){
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        Page<Promotion> promotionPage = promotionService.getAllPromotions(page, size, keyword, status);

        model.addAttribute("totalPromotions", promotionService.countAll());
        model.addAttribute("activeCount", promotionService.countActive());
        model.addAttribute("inactiveCount", promotionService.countInactive());
        model.addAttribute("totalDiscountedAmount", "0 đ");

        model.addAttribute("promotions", promotionPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotionPage.getTotalPages());
        model.addAttribute("totalElements", promotionPage.getTotalElements());
        model.addAttribute("promotionBasePath", "/admin/promotions");

        return "promotion-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("promotionBasePath", "/admin/promotions");
        return "promotion-create";
    }

    @PostMapping("/create-action")
    public String createPromotion(@ModelAttribute Promotion promotion,
                                  Model model,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            promotionService.create(promotion);
            // Nếu thành công, dùng FlashAttribute để hiện thông báo ở trang List
            redirectAttributes.addFlashAttribute("success", "Thêm khuyến mãi thành công!");
            return "redirect:/admin/promotions/view";

        } catch (RuntimeException e) {
            // LỖI NGHIỆP VỤ (Trùng mã, v.v.): Trả về trang form ngay lập tức
            model.addAttribute("error", e.getMessage());
            model.addAttribute("promotion", promotion); // Giữ lại dữ liệu đã nhập
            model.addAttribute("promotionBasePath", "/admin/promotions");
            return "promotion-create";

        } catch (Exception e) {
            // LỖI HỆ THỐNG
            log.error("Lỗi khi tạo khuyến mãi: ", e);
            model.addAttribute("error", "Có lỗi hệ thống xảy ra!");
            model.addAttribute("promotion", promotion);
            model.addAttribute("promotionBasePath", "/admin/promotions");
            return "promotion-create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        try {
            Promotion promotion = promotionService.findById(id);
            model.addAttribute("promotion", promotion);
            model.addAttribute("promotionBasePath", "/admin/promotions");
            return "promotion-edit";
        } catch (RuntimeException e) {
            return "redirect:/admin/promotions/view";
        }
    }

    @PostMapping("/update-action")
    public String updatePromotion(@ModelAttribute Promotion promotion,
                                  Model model,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        try {
            promotionService.update(promotion);
            return "redirect:/admin/promotions/view";
        }catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("promotion", promotion);
            model.addAttribute("promotionBasePath", "/admin/promotions");
            return "promotion-edit";
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật khuyến mãi: ", e);
            model.addAttribute("error", "Có lỗi hệ thống xảy ra!");
            model.addAttribute("promotionBasePath", "/admin/promotions");
            return "promotion-edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Integer id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = requireAdmin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }
        promotionService.softDelete(id);
        return "redirect:/admin/promotions/view";
    }

    private User requireAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return null;
        }
        return user;
    }
    // API endpoint for validating promo code
    @GetMapping("/api/validate")
    @ResponseBody
    public Map<String, Object> validatePromoCode(@RequestParam String code, @RequestParam BigDecimal amount) {
        return promotionService.validatePromoCode(code, amount);
    }
}