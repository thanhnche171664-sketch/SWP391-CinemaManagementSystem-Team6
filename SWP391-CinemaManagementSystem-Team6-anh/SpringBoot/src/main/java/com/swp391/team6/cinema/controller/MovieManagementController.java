package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.MovieDTO;
import com.swp391.team6.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/movies")
@RequiredArgsConstructor
public class MovieManagementController {

    private final MovieService movieService;

    @GetMapping
    public String list(Model model) {
        List<MovieDTO> movies = movieService.getAllMovies(true);
        model.addAttribute("movieList", movies);
        model.addAttribute("newMovie", new MovieDTO());
        return "movie-management";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("newMovie") MovieDTO movieDTO) {
        Long movieId = movieDTO.getMovieId();
        if (movieId == null || movieId <= 0) {
            movieService.createMovie(movieDTO);
        } else {
            movieService.updateMovie(movieId, movieDTO);
        }
        return "redirect:/admin/movies";
    }

    @GetMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id) {
        MovieDTO movie = movieService.getMovieById(id, true);
        boolean hidden = Boolean.TRUE.equals(movie.getHidden());
        movieService.updateVisibility(id, !hidden);
        return "redirect:/admin/movies";
    }
}
