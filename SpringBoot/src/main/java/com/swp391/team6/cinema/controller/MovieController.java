package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.MovieDTO;
import com.swp391.team6.cinema.view.ApiResponse;
import com.swp391.team6.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieDTO>>> getAllMovies(
            @RequestParam(defaultValue = "false") boolean includeHidden) {
        try {
            List<MovieDTO> movies = movieService.getAllMovies(includeHidden);
            return ResponseEntity.ok(ApiResponse.success("Get movies successfully", movies));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> getMovieById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeHidden) {
        try {
            MovieDTO movie = movieService.getMovieById(id, includeHidden);
            return ResponseEntity.ok(ApiResponse.success("Get movie successfully", movie));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MovieDTO>> createMovie(@RequestBody MovieDTO movieDTO) {
        try {
            MovieDTO createdMovie = movieService.createMovie(movieDTO);
            return ResponseEntity.ok(ApiResponse.success("Create movie successfully", createdMovie));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> updateMovie(
            @PathVariable Long id,
            @RequestBody MovieDTO movieDTO) {
        try {
            MovieDTO updatedMovie = movieService.updateMovie(id, movieDTO);
            return ResponseEntity.ok(ApiResponse.success("Update movie successfully", updatedMovie));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<ApiResponse<MovieDTO>> updateVisibility(
            @PathVariable Long id,
            @RequestParam boolean hidden) {
        try {
            MovieDTO updatedMovie = movieService.updateVisibility(id, hidden);
            return ResponseEntity.ok(ApiResponse.success("Update movie visibility successfully", updatedMovie));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
