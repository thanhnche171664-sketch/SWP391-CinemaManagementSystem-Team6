package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(Movie.MovieStatus status);

    @Query("SELECT m FROM Movie m WHERE m.title LIKE %:keyword%")
    List<Movie> searchByTitle(@Param("keyword") String keyword);
    
    List<Movie> findByIsHiddenFalse();

    Page<Movie> findByIsHiddenFalse(Pageable pageable);

    Optional<Movie> findByTitleIgnoreCase(String title);

    List<Movie> findByStatusAndIsHiddenFalse(Movie.MovieStatus status);
}
