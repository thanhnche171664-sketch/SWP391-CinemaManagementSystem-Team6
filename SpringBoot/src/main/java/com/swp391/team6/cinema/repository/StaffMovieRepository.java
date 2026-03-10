package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StaffMovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByStatus(Movie.MovieStatus status);
}