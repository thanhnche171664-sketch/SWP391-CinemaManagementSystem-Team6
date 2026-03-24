package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(Movie.MovieStatus status);

    List<Movie> findByIsHiddenFalse();

    Page<Movie> findByIsHiddenFalse(Pageable pageable);

    Optional<Movie> findByTitleIgnoreCase(String title);

    List<Movie> findByStatusAndIsHiddenFalse(Movie.MovieStatus status);

    @Query("SELECT m FROM Movie m " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR m.status = :status) " +
           "AND (:hidden IS NULL OR m.isHidden = :hidden)")
    Page<Movie> findAdminMoviesWithFilters(@Param("keyword") String keyword,
                                           @Param("status") Movie.MovieStatus status,
                                           @Param("hidden") Boolean hidden,
                                           Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres " +
           "WHERE m.isHidden = false " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR m.status = :status) " +
           "AND (:genreName IS NULL OR :genreName = '' OR m IN (SELECT m2 FROM Movie m2 JOIN m2.genres g2 WHERE g2.genreName = :genreName))")
    List<Movie> findVisibleMoviesWithFilters(@Param("keyword") String keyword,
                                             @Param("status") Movie.MovieStatus status,
                                             @Param("genreName") String genreName);

    @Query("SELECT DISTINCT m FROM Movie m JOIN BranchMovie bm ON m.movieId = bm.movie.movieId WHERE bm.branch.branchId = :branchId AND m.status = 'now_showing'")
    List<Movie> findMoviesByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN BranchMovie bm ON m.movieId = bm.movie.movieId " +
            "LEFT JOIN MovieGenre mg ON m.movieId = mg.movie.movieId " +
            "LEFT JOIN Genre g ON mg.genre.genreId = g.genreId " +
            "WHERE bm.branch.branchId = :branchId " +
            "AND m.isHidden = false " +
            "AND (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR m.status = :status) " +
            "AND (:genre IS NULL OR g.genreName = :genre)")
    Page<Movie> findMoviesForPOS(
            @Param("branchId") Long branchId,
            @Param("keyword") String keyword,
            @Param("status") Movie.MovieStatus status,
            @Param("genre") String genre,
            Pageable pageable
    );

    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN MovieGenre mg ON m.movieId = mg.movie.movieId " +
            "LEFT JOIN Genre g ON mg.genre.genreId = g.genreId " +
            "WHERE m.isHidden = false " +
            "AND (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR m.status = :status) " +
            "AND (:genre IS NULL OR g.genreName = :genre)")
    Page<Movie> findMoviesForPOSAllBranches(
            @Param("keyword") String keyword,
            @Param("status") Movie.MovieStatus status,
            @Param("genre") String genre,
            Pageable pageable
    );

    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN BranchMovie bm ON m.movieId = bm.movie.movieId " +
            "LEFT JOIN MovieGenre mg ON m.movieId = mg.movie.movieId " +
            "LEFT JOIN Genre g ON mg.genre.genreId = g.genreId " +
            "WHERE bm.branch.branchId = :branchId " +
            "AND (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR m.status = :status) " +
            "AND (:hidden IS NULL OR m.isHidden = :hidden)")
    Page<Movie> findBranchMoviesForManagement(
            @Param("branchId") Long branchId,
            @Param("keyword") String keyword,
            @Param("status") Movie.MovieStatus status,
            @Param("hidden") Boolean hidden,
            Pageable pageable
    );
}
