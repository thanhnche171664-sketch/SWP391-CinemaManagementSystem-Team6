package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.MovieDTO;
import com.swp391.team6.cinema.entity.Genre;
import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.repository.GenreRepository;
import com.swp391.team6.cinema.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public List<Movie> getVisibleMovies() {
        List<Movie> movies = movieRepository.findByIsHiddenFalse();
        movies.forEach(movie -> movie.getGenres().size());
        return movies;
    }

    @Transactional(readOnly = true)
    public List<Movie> getVisibleMoviesWithFilters(String keyword, Movie.MovieStatus status, String genreName) {
        List<Movie> movies = movieRepository.findVisibleMoviesWithFilters(
                (keyword != null && !keyword.isBlank()) ? keyword.trim() : null,
                status,
                (genreName != null && !genreName.isBlank()) ? genreName.trim() : null);
        movies.forEach(movie -> movie.getGenres().size());
        return movies;
    }

    @Transactional(readOnly = true)
    public List<Movie> getVisibleMoviesByStatus(Movie.MovieStatus status) {
        List<Movie> movies = movieRepository.findByStatusAndIsHiddenFalse(status);
        movies.forEach(movie -> movie.getGenres().size());
        return movies;
    }

    @Transactional(readOnly = true)
    public Movie getVisibleMovieById(Long id) {
        Movie movie = getMovieEntityById(id);
        movie.getGenres().size();
        if (Boolean.TRUE.equals(movie.getIsHidden())) {
            throw new RuntimeException("Movie is hidden with id: " + id);
        }
        return movie;
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getAllMovies(boolean includeHidden) {
        List<Movie> movies = includeHidden ? movieRepository.findAll() : movieRepository.findByIsHiddenFalse();
        return movies.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MovieDTO getMovieById(Long id, boolean includeHidden) {
        Movie movie = getMovieEntityById(id);
        if (!includeHidden && Boolean.TRUE.equals(movie.getIsHidden())) {
            throw new RuntimeException("Movie is hidden with id: " + id);
        }
        return convertToDTO(movie);
    }

    @Transactional
    public MovieDTO createMovie(MovieDTO movieDTO) {
        Movie movie = convertToEntity(movieDTO);
        movie.setGenres(resolveGenres(movieDTO.getGenreIds()));
        // Ensure new entity is always inserted
        movie.setMovieId(null);
        Movie savedMovie = movieRepository.save(movie);
        return convertToDTO(savedMovie);
    }

    @Transactional
    public MovieDTO updateMovie(Long id, MovieDTO movieDTO) {
        Movie existingMovie = getMovieEntityById(id);

        if (movieDTO.getTitle() != null) {
            existingMovie.setTitle(movieDTO.getTitle());
        }
        if (movieDTO.getDescription() != null) {
            existingMovie.setDescription(movieDTO.getDescription());
        }
        if (movieDTO.getDuration() != null) {
            existingMovie.setDuration(movieDTO.getDuration());
        }
        if (movieDTO.getGenreIds() != null) {
            existingMovie.setGenres(resolveGenres(movieDTO.getGenreIds()));
        }
        if (movieDTO.getAgeRating() != null) {
            existingMovie.setAgeRating(movieDTO.getAgeRating());
        }
        if (movieDTO.getPosterUrl() != null) {
            existingMovie.setPosterUrl(movieDTO.getPosterUrl());
        }
        if (movieDTO.getTrailerUrl() != null) {
            existingMovie.setTrailerUrl(movieDTO.getTrailerUrl());
        }
        if (movieDTO.getStatus() != null) {
            existingMovie.setStatus(movieDTO.getStatus());
        }
        if (movieDTO.getHidden() != null) {
            existingMovie.setIsHidden(movieDTO.getHidden());
        }

        Movie updatedMovie = movieRepository.save(existingMovie);
        return convertToDTO(updatedMovie);
    }

    @Transactional
    public MovieDTO updateVisibility(Long id, boolean hidden) {
        Movie movie = getMovieEntityById(id);
        movie.setIsHidden(hidden);
        return convertToDTO(movieRepository.save(movie));
    }

    private Movie getMovieEntityById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
    }

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setMovieId(movie.getMovieId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDuration(movie.getDuration());
        dto.setAgeRating(movie.getAgeRating());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setStatus(movie.getStatus());
        dto.setHidden(movie.getIsHidden());
        dto.setGenreIds(movie.getGenres()
                .stream()
                .map(Genre::getGenreId)
                .collect(Collectors.toList()));
        dto.setGenreNames(movie.getGenreNames());
        return dto;
    }

    private Movie convertToEntity(MovieDTO dto) {
        Movie movie = new Movie();
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setDuration(dto.getDuration());
        movie.setAgeRating(dto.getAgeRating());
        movie.setPosterUrl(dto.getPosterUrl());
        movie.setTrailerUrl(dto.getTrailerUrl());
        if (dto.getStatus() != null) {
            movie.setStatus(dto.getStatus());
        }
        if (dto.getHidden() != null) {
            movie.setIsHidden(dto.getHidden());
        }
        return movie;
    }

    private List<Genre> resolveGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new ArrayList<>();
        }
        return genreRepository.findAllById(genreIds);
    }
}
