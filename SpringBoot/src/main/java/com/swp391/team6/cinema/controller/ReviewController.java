package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public String addReview(@RequestParam Long bookingId,
                            @RequestParam int rating,
                            @RequestParam String comment,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        // Lấy user từ session (key là "loggedInUser" như trong code trước của bạn)
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/auth/login";
        }

        String result = reviewService.addReview(user, bookingId, rating, comment);

        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Cảm ơn bạn đã đánh giá phim!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        // Quay lại trang lịch sử đặt vé
        return "redirect:/customer/history";
    }
}