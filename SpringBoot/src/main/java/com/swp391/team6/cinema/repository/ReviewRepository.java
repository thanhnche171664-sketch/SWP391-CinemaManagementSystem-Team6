package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByMovieMovieId(Long movieId);
    
    List<Review> findByUserUserId(Long userId);
    
    List<Review> findByStatus(Review.ReviewStatus status);
}
