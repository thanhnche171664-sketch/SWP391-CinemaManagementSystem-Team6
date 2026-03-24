package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMovieMovieId(Long movieId);

    List<Review> findByUserUserId(Long userId);

    List<Review> findByStatus(Review.ReviewStatus status);

    List<Review> findByMovie_MovieId(Long movieId);

    @Query("""
        SELECT r FROM Review r
        WHERE (:movieId IS NULL OR r.movie.movieId = :movieId)
        AND (:keyword IS NULL OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Review> filterReviews(
            @Param("movieId") Long movieId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
