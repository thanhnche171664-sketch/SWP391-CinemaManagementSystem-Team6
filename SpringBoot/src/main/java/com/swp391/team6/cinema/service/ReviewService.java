package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.Review;
import com.swp391.team6.cinema.repository.BookingRepository;
import com.swp391.team6.cinema.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    // lấy danh sách review theo movie
    public List<Review> getReviewsByMovieId(Long movieId) {
        return reviewRepository.findByMovie_MovieId(movieId);
    }

    public void saveReview(Review review) {
        if (review.getUser() == null || review.getMovie() == null) {
            throw new RuntimeException("Dữ liệu không hợp lệ!");
        }

        Long userId = review.getUser().getUserId();
        Long movieId = review.getMovie().getMovieId();
        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(120);

        // Đã sửa thành Booking.BookingStatus.paid để khớp với Entity của bạn
        boolean canReview = bookingRepository.existsByUserUserIdAndShowtimeMovieMovieIdAndStatusAndShowtimeStartTimeBefore(
                userId,
                movieId,
                Booking.BookingStatus.paid,
                thresholdTime
        );

        if (!canReview) {
            throw new RuntimeException("Bạn chỉ có thể đánh giá sau khi đã mua vé và phim đã chiếu xong!");
        }

        review.setStatus(Review.ReviewStatus.approved);
        review.setCreatedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    public Page<Review> getReviews(Long movieId, String keyword, int page) {

        Pageable pageable = PageRequest.of(page, 7, Sort.by("createdAt").descending());

        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        return reviewRepository.filterReviews(movieId, keyword, pageable);
    }

}
