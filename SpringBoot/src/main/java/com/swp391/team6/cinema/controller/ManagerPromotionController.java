package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.Promotion;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.service.PromotionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/manager/promotions")
@RequiredArgsConstructor
public class ManagerPromotionController {

    private static final Logger log = LoggerFactory.getLogger(ManagerPromotionController.class);

    private final PromotionService promotionService;
    private final CinemaBranchRepository cinemaBranchRepository;

    @GetMapping("/view")
    public String viewPromotions(Model model,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String discountType,
                                 @RequestParam(required = false) String timeFilter) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Page<Promotion> promotionPage = promotionService.getPromotionsForBranch(
                user.getBranchId(), page, size, keyword, status, discountType, timeFilter
        );
        List<Promotion> branchPromotions = promotionService.getPromotionsByBranch(user.getBranchId());
        applyExpiryStatus(branchPromotions);

        model.addAttribute("totalPromotions", branchPromotions.size());
        model.addAttribute("activeCount", branchPromotions.stream().filter(p -> p.getStatus() == Promotion.Status.active).count());
        model.addAttribute("inactiveCount", branchPromotions.stream().filter(p -> p.getStatus() == Promotion.Status.inactive).count());
        model.addAttribute("totalDiscountedAmount", "0 đ");

        model.addAttribute("promotions", promotionPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("discountType", discountType);
        model.addAttribute("timeFilter", timeFilter);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotionPage.getTotalPages());
        model.addAttribute("totalElements", promotionPage.getTotalElements());
        model.addAttribute("promotionBasePath", "/manager/promotions");

        return "promotion-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("promotion", new Promotion());
        model.addAttribute("promotionBasePath", "/manager/promotions");
        return "promotion-create";
    }

    @PostMapping("/create-action")
    public String createPromotion(@ModelAttribute Promotion promotion,
                                  Model model,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            CinemaBranch branch = cinemaBranchRepository.findById(user.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Chi nhánh của manager không tồn tại."));
            promotion.setBranch(branch);
            promotionService.create(promotion);
            return "redirect:/manager/promotions/view";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("promotion", promotion);
            model.addAttribute("promotionBasePath", "/manager/promotions");
            return "promotion-create";
        } catch (Exception e) {
            log.error("Lỗi khi tạo khuyến mãi: ", e);
            model.addAttribute("error", "Có lỗi hệ thống xảy ra!");
            model.addAttribute("promotionBasePath", "/manager/promotions");
            return "promotion-create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            Promotion promotion = promotionService.findById(id);
            if (!belongsToBranch(promotion, user.getBranchId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập khuyến mãi này.");
                return "redirect:/manager/promotions/view";
            }
            model.addAttribute("promotion", promotion);
            model.addAttribute("promotionBasePath", "/manager/promotions");
            return "promotion-edit";
        } catch (RuntimeException e) {
            return "redirect:/manager/promotions/view";
        }
    }

    @PostMapping("/update-action")
    public String updatePromotion(@ModelAttribute Promotion promotion,
                                  Model model,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            Promotion existing = promotionService.findById(promotion.getPromotionId());
            if (!belongsToBranch(existing, user.getBranchId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền cập nhật khuyến mãi này.");
                return "redirect:/manager/promotions/view";
            }
            promotion.setBranch(existing.getBranch());
            promotionService.update(promotion);
            return "redirect:/manager/promotions/view";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("promotion", promotion);
            model.addAttribute("promotionBasePath", "/manager/promotions");
            return "promotion-edit";
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật khuyến mãi: ", e);
            model.addAttribute("error", "Có lỗi hệ thống xảy ra!");
            model.addAttribute("promotionBasePath", "/manager/promotions");
            return "promotion-edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Integer id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Promotion promotion = promotionService.findById(id);
        if (!belongsToBranch(promotion, user.getBranchId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền tạm dừng khuyến mãi này.");
            return "redirect:/manager/promotions/view";
        }

        promotionService.softDelete(id);
        return "redirect:/manager/promotions/view";
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

    private boolean belongsToBranch(Promotion promotion, Long branchId) {
        return promotion.getBranch() != null
                && promotion.getBranch().getBranchId() != null
                && promotion.getBranch().getBranchId().equals(branchId);
    }

    private void applyExpiryStatus(List<Promotion> promotions) {
        LocalDateTime now = LocalDateTime.now();
        promotions.forEach(p -> {
            if (p.getEndDate() != null && p.getEndDate().isBefore(now)) {
                p.setStatus(Promotion.Status.inactive);
            }
        });
    }
}
