package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByGenreNameIgnoreCase(String genreName);

    boolean existsByGenreNameIgnoreCase(String genreName);

    List<Genre> findAllByOrderByGenreNameAsc();
}
