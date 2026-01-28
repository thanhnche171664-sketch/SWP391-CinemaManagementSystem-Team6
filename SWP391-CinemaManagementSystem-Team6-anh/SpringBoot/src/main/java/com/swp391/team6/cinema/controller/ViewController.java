package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final MovieService movieService;

    @GetMapping("/")
    public String home(Model model) {
        List<Movie> movies = movieService.getVisibleMoviesByStatus(Movie.MovieStatus.now_showing);
        model.addAttribute("movies", movies);
        return "index";
    }

    @GetMapping("/movies")
    public String movies(Model model) {
        List<Movie> movies = movieService.getVisibleMovies();
        model.addAttribute("movies", movies);
        return "movies";
    }

    @GetMapping("/movies/{id}")
    public String movieDetail(@PathVariable Long id, Model model) {
        Movie movie = movieService.getVisibleMovieById(id);
        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", List.of());
        return "movie-detail";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
