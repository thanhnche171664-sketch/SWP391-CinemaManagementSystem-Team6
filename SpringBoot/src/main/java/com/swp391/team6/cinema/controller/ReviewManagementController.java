package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Review;
import com.swp391.team6.cinema.service.MovieService;
import com.swp391.team6.cinema.service.ReviewService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reviews")
public class ReviewManagementController {
    private final ReviewService reviewService;
    private final MovieService movieService;

    public ReviewManagementController(ReviewService reviewService,
                                 MovieService movieService) {
        this.reviewService = reviewService;
        this.movieService = movieService;
    }

    @GetMapping
    public String reviewList(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Review.ReviewStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        Page<Review> reviewPage = reviewService.getReviews(movieId, keyword, rating, status, fromDate, toDate, page);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("listReviews", reviewPage.getContent());
        model.addAttribute("listMovies", movieService.getAllMovies());

        model.addAttribute("movieId", movieId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("rating", rating);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "manage-reviews";
    }
}
