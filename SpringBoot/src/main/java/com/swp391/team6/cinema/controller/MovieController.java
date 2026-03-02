package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.entity.Showtime;
import com.swp391.team6.cinema.service.BookingService;
import com.swp391.team6.cinema.service.MovieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class MovieController {

    private final MovieService movieService;
    private final BookingService bookingService;

    public MovieController(MovieService movieService, BookingService bookingService) {
        this.movieService = movieService;
        this.bookingService = bookingService;
    }

    @GetMapping({"/", "/movies"})
    public String listMovies(Model model) {
        List<Movie> movies = movieService.getMoviesNowShowing();
        model.addAttribute("movies", movies);
        return "movies";
    }

    @GetMapping("/movies/{id}")
    public String movieDetail(@PathVariable Long id, Model model) {
        Movie movie = movieService.getMovieById(id);
        List<Showtime> showtimes = bookingService.getShowtimesByMovie(id);
        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimes);
        return "movie-detail";
    }
}
