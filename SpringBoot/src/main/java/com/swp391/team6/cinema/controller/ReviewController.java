package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Review;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.UserRepository;
import com.swp391.team6.cinema.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Xử lý thêm đánh giá mới
     */
    @PostMapping("/add")
    public String addReview(@ModelAttribute("review") Review review,
                            HttpSession session,
                            RedirectAttributes ra) {

        // 1. Lấy user từ Session
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập để để lại đánh giá.");
            return "redirect:/auth/login";
        }

        // 2. Kiểm tra an toàn dữ liệu phim
        if (review.getMovie() == null || review.getMovie().getMovieId() == null) {
            ra.addFlashAttribute("error", "Không tìm thấy thông tin phim để đánh giá.");
            return "redirect:/movies";
        }

        Long movieId = review.getMovie().getMovieId();

        try {
            // 3. Gán user và gọi Service để xử lý logic (paid, threshold time,...)
            review.setUser(userRepository.getReferenceById(loggedInUser.getUserId()));
            reviewService.saveReview(review);

            ra.addFlashAttribute("message", "Cảm ơn bạn! Đánh giá của bạn đã được đăng tải.");

        } catch (RuntimeException e) {
            // Bắt lỗi: "Bạn chỉ có thể đánh giá sau khi đã mua vé và phim đã kết thúc!"
            ra.addFlashAttribute("error", e.getMessage());
        }

        // 4. Quay lại trang chi tiết phim
        return "redirect:/movies/" + movieId + "#review-section";
    }

}