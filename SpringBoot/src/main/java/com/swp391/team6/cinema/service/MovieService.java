package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getMoviesNowShowing() {
        return movieRepository.findByStatus(Movie.MovieStatus.now_showing);
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim với id: " + id));
    }
}
