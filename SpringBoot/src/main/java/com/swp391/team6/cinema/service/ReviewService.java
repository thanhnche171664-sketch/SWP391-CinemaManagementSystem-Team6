package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.entity.Review;
import com.swp391.team6.cinema.repository.BookingRepository;
import com.swp391.team6.cinema.repository.MovieRepository;
import com.swp391.team6.cinema.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final MovieRepository movieRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository, MovieRepository movieRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.movieRepository = movieRepository;
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
        Movie movie = movieRepository.getReferenceById(movieId);
        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(movie.getDuration());

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

    public Page<Review> getReviews(Long movieId,
                                   String keyword,
                                   Integer rating,
                                   Review.ReviewStatus status,
                                   LocalDate fromDate,
                                   LocalDate toDate,
                                   int page) {

        Pageable pageable = PageRequest.of(page, 7, Sort.by("createdAt").descending());

        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(LocalTime.MAX) : null;

        return reviewRepository.filterReviews(
                movieId,
                keyword,
                rating,
                status,
                fromDateTime,
                toDateTime,
                pageable
        );
    }

}
