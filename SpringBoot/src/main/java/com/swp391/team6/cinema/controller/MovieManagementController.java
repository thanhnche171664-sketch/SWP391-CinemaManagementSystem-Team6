package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.MovieDTO;
import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.GenreService;
import com.swp391.team6.cinema.service.MovieService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/movies")
@RequiredArgsConstructor
public class MovieManagementController {

    private final MovieService movieService;
    private final GenreService genreService;

    @GetMapping
    public String list(Model model,
                       HttpSession session,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "8") int size,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String hidden,
                       @RequestParam(required = false) String sort) {
        Movie.MovieStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = Movie.MovieStatus.valueOf(status.trim());
            } catch (IllegalArgumentException ignored) {
                statusFilter = null;
            }
        }

        Boolean hiddenFilter = null;
        if (hidden != null && !hidden.isBlank()) {
            if ("shown".equalsIgnoreCase(hidden)) {
                hiddenFilter = false;
            } else if ("hidden".equalsIgnoreCase(hidden)) {
                hiddenFilter = true;
            }
        }

        Page<MovieDTO> moviePage = movieService.getMoviesPageWithFilters(
                page,
                size,
                search,
                statusFilter,
                hiddenFilter,
                sort);
        model.addAttribute("movieList", moviePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", moviePage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("statusFilter", status == null ? "" : status);
        model.addAttribute("hiddenFilter", hidden == null ? "" : hidden);
        model.addAttribute("sortFilter", sort == null ? "" : sort);
        model.addAttribute("newMovie", new MovieDTO());
        model.addAttribute("genreList", genreService.getAllGenres());
        model.addAttribute("branches", movieService.getActiveBranches());
        model.addAttribute("readOnlyMode", false);
        model.addAttribute("movieBasePath", "/admin/movies");
        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", user);
        return "movie-management";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("newMovie") MovieDTO movieDTO,
                       RedirectAttributes redirectAttributes) {
        try {
            Long movieId = movieDTO.getMovieId();
            if (movieId == null || movieId <= 0) {
                movieService.createMovie(movieDTO);
                redirectAttributes.addFlashAttribute("success", "Thêm phim thành công.");
            } else {
                movieService.updateMovie(movieId, movieDTO);
                redirectAttributes.addFlashAttribute("success", "Cập nhật phim thành công.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
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

    @GetMapping("/view/{id}")
    public String viewMovie(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        MovieDTO movie = movieService.getMovieById(id, true);
        if (Boolean.TRUE.equals(movie.getHidden())) {
            redirectAttributes.addFlashAttribute("error", "Phim đã bị ẩn.");
            return "redirect:/admin/movies";
        }
        return "redirect:/movies/" + id;
    }
}
