package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.MovieDTO;
import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public List<Movie> getVisibleMovies() {
        return movieRepository.findByIsHiddenFalse();
    }

    public List<Movie> getVisibleMoviesByStatus(Movie.MovieStatus status) {
        return movieRepository.findByStatusAndIsHiddenFalse(status);
    }

    public Movie getVisibleMovieById(Long id) {
        Movie movie = getMovieEntityById(id);
        if (Boolean.TRUE.equals(movie.getIsHidden())) {
            throw new RuntimeException("Movie is hidden with id: " + id);
        }
        return movie;
    }

    public List<MovieDTO> getAllMovies(boolean includeHidden) {
        List<Movie> movies = includeHidden ? movieRepository.findAll() : movieRepository.findByIsHiddenFalse();
        return movies.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

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
        if (movieDTO.getGenre() != null) {
            existingMovie.setGenre(movieDTO.getGenre());
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
        dto.setGenre(movie.getGenre());
        dto.setAgeRating(movie.getAgeRating());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setStatus(movie.getStatus());
        dto.setHidden(movie.getIsHidden());
        return dto;
    }

    private Movie convertToEntity(MovieDTO dto) {
        Movie movie = new Movie();
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setDuration(dto.getDuration());
        movie.setGenre(dto.getGenre());
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
}
