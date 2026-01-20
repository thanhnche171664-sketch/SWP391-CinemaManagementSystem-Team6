//package com.swp391.team6.cinema.service;
//
//import com.swp391.team6.cinema.dto.MovieDTO;
//import com.swp391.team6.cinema.entity.Movie;
//import com.swp391.team6.cinema.repository.MovieRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class MovieService {
//
//    private final MovieRepository movieRepository;
//
//    public List<Movie> getAllMovies() {
//        return movieRepository.findAll();
//    }
//
//    public Movie getMovieById(Long id) {
//        return movieRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
//    }
//
//    public List<Movie> getMoviesByStatus(Movie.MovieStatus status) {
//        return movieRepository.findByStatus(status);
//    }
//
//    public List<MovieDTO> searchMovies(String keyword) {
//        return movieRepository.searchByTitle(keyword)
//                .stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public MovieDTO createMovie(MovieDTO movieDTO) {
//        Movie movie = convertToEntity(movieDTO);
//        Movie savedMovie = movieRepository.save(movie);
//        return convertToDTO(savedMovie);
//    }
//
//    @Transactional
//    public MovieDTO updateMovie(Long id, MovieDTO movieDTO) {
//        Movie existingMovie = movieRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
//
//        existingMovie.setTitle(movieDTO.getTitle());
//        existingMovie.setDescription(movieDTO.getDescription());
//        existingMovie.setDuration(movieDTO.getDuration());
//        existingMovie.setGenre(movieDTO.getGenre());
//        existingMovie.setDirector(movieDTO.getDirector());
//        existingMovie.setPosterUrl(movieDTO.getPosterUrl());
//        existingMovie.setStatus(movieDTO.getStatus());
//
//        Movie updatedMovie = movieRepository.save(existingMovie);
//        return convertToDTO(updatedMovie);
//    }
//
//    @Transactional
//    public void deleteMovie(Long id) {
//        Movie movie = movieRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
//
//        movie.setIsDeleted(true);
//        movieRepository.save(movie);
//    }
//
//    // Helper methods for conversion
//    private MovieDTO convertToDTO(Movie movie) {
//        MovieDTO dto = new MovieDTO();
//        dto.setId(movie.getId());
//        dto.setTitle(movie.getTitle());
//        dto.setDescription(movie.getDescription());
//        dto.setDuration(movie.getDuration());
//        dto.setGenre(movie.getGenre());
//        dto.setDirector(movie.getDirector());
//        dto.setPosterUrl(movie.getPosterUrl());
//        dto.setStatus(movie.getStatus());
//        return dto;
//    }
//
//    private Movie convertToEntity(MovieDTO dto) {
//        Movie movie = new Movie();
//        movie.setTitle(dto.getTitle());
//        movie.setDescription(dto.getDescription());
//        movie.setDuration(dto.getDuration());
//        movie.setGenre(dto.getGenre());
//        movie.setDirector(dto.getDirector());
//        movie.setPosterUrl(dto.getPosterUrl());
//        movie.setStatus(dto.getStatus());
//        return movie;
//    }
//}
