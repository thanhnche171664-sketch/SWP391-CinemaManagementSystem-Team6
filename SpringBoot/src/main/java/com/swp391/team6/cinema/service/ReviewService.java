package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.Review;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.BookingRepository;
import com.swp391.team6.cinema.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public String addReview(User user, Long bookingId, int rating, String comment) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null || !booking.getUser().getUserId().equals(user.getUserId())) {
            return "Giao dịch không hợp lệ!";
        }

        // SỬA TẠI ĐÂY: Dùng .paid thay vì .SUCCESS
        if (booking.getStatus() != Booking.BookingStatus.paid) {
            return "Bạn chưa hoàn tất thanh toán cho bộ phim này!";
        }

        if (LocalDateTime.now().isBefore(booking.getShowtime().getEndTime())) {
            return "Phim chưa kết thúc, vui lòng đánh giá sau khi xem xong!";
        }

        Review review = new Review();
        review.setUser(user);
        review.setMovie(booking.getShowtime().getMovie());
        review.setRating(rating);
        review.setComment(comment);
        review.setStatus(Review.ReviewStatus.pending);
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);
        return "SUCCESS"; // Đây là chuỗi String trả về cho Controller, giữ nguyên cũng được
    }
}
