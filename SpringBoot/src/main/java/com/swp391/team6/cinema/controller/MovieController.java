//package com.swp391.team6.cinema.controller;
//
//import com.swp391.team6.cinema.dto.MovieDTO;
//import com.swp391.team6.cinema.service.MovieService;
//import com.swp391.team6.cinema.view.ApiResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/movies")
//@RequiredArgsConstructor
//public class MovieController {
//
//    private final MovieService movieService;
//
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<MovieDTO>>> getAllMovies() {
//        try {
//            List<MovieDTO> movies = movieService.getAllMovies();
//            return ResponseEntity.ok(ApiResponse.success("Get all movies successfully", movies));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<MovieDTO>> getMovieById(@PathVariable Long id) {
//        try {
//            MovieDTO movie = movieService.getMovieById(id);
//            return ResponseEntity.ok(ApiResponse.success("Get movie successfully", movie));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    @GetMapping("/status/{status}")
//    public ResponseEntity<ApiResponse<List<MovieDTO>>> getMoviesByStatus(@PathVariable String status) {
//        try {
//            List<MovieDTO> movies = movieService.getMoviesByStatus(status);
//            return ResponseEntity.ok(ApiResponse.success("Get movies by status successfully", movies));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    @GetMapping("/search")
//    public ResponseEntity<ApiResponse<List<MovieDTO>>> searchMovies(@RequestParam String keyword) {
//        try {
//            List<MovieDTO> movies = movieService.searchMovies(keyword);
//            return ResponseEntity.ok(ApiResponse.success("Search movies successfully", movies));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    @PostMapping
//    public ResponseEntity<ApiResponse<MovieDTO>> createMovie(@RequestBody MovieDTO movieDTO) {
//        try {
//            MovieDTO createdMovie = movieService.createMovie(movieDTO);
//            return ResponseEntity.ok(ApiResponse.success("Create movie successfully", createdMovie));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse<MovieDTO>> updateMovie(
//            @PathVariable Long id,
//            @RequestBody MovieDTO movieDTO) {
//        try {
//            MovieDTO updatedMovie = movieService.updateMovie(id, movieDTO);
//            return ResponseEntity.ok(ApiResponse.success("Update movie successfully", updatedMovie));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable Long id) {
//        try {
//            movieService.deleteMovie(id);
//            return ResponseEntity.ok(ApiResponse.success("Delete movie successfully", null));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//}
