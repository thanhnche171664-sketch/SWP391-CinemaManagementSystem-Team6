package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(Movie.MovieStatus status);

    @Query("SELECT m FROM Movie m WHERE m.title LIKE %:keyword%")
    List<Movie> searchByTitle(@Param("keyword") String keyword);
    
    List<Movie> findByGenre(String genre);

    List<Movie> findByIsHiddenFalse();

    List<Movie> findByStatusAndIsHiddenFalse(Movie.MovieStatus status);
}
