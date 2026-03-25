package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Review;
import com.swp391.team6.cinema.service.MovieService;
import com.swp391.team6.cinema.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        Page<Review> reviewPage = reviewService.getReviews(movieId, keyword, page);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("listReviews", reviewPage.getContent());
        model.addAttribute("listMovies", movieService.getAllMovies());

        model.addAttribute("movieId", movieId);
        model.addAttribute("keyword", keyword);

        return "manage-reviews";
    }
}
