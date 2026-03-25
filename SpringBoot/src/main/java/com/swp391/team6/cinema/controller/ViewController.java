package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.repository.ShowtimeRepository;
import com.swp391.team6.cinema.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final MovieService movieService;
    private final GenreService genreService;
    private final ShowtimeRepository showtimeRepository;
    private final NewsService newsService;
    private final CinemaBranchRepository cinemaBranchRepository;
    private final AdminDashboardService adminDashboardService;
    private final ReviewService reviewService;
    private final CinemaBranchService cinemaBranchService;

    @GetMapping("/")
    public String home(Model model) {
        List<Movie> movies = movieService.getLatestVisibleMoviesByStatus(Movie.MovieStatus.now_showing, 6);
        model.addAttribute("latestNews", newsService.getLatestPublishedNews(6));
        model.addAttribute("movies", movies);
        return "index";
    }
    
    @GetMapping("/home")
    public String customerHome(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/auth/login";
        }
        
        List<Movie> movies = movieService.getLatestVisibleMoviesByStatus(Movie.MovieStatus.now_showing, 6);
        model.addAttribute("latestNews", newsService.getLatestPublishedNews(6));
        model.addAttribute("movies", movies);
        model.addAttribute("user", user);
        return "index";
    }
    
    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }

        AdminDashboardService.AdminDashboardData dashboard = adminDashboardService.buildDashboard();
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("user", user);
        return "admin-dashboard";
    }
    
    @GetMapping("/manager")
    public String managerDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || (user.getRole() != User.UserRole.MANAGER && user.getRole() != User.UserRole.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }
        
        model.addAttribute("user", user);
        return "redirect:/manager/dashboard";
    }
    
    @GetMapping("/staff")
    public String staffDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || (user.getRole() != User.UserRole.STAFF && 
                            user.getRole() != User.UserRole.MANAGER && 
                            user.getRole() != User.UserRole.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }
        
        model.addAttribute("user", user);
        if (user.getRole() == User.UserRole.STAFF) {
            return "redirect:/staff/booking/pos";
        }
        return "redirect:/admin/staff";
    }

    @GetMapping("/movies")
    public String movies(@RequestParam(required = false) String search,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String genre,
                         @RequestParam(required = false) Long branchId,
                         Model model) {
        Movie.MovieStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = Movie.MovieStatus.valueOf(status.trim().toLowerCase());
            } catch (IllegalArgumentException ignored) {}
        }
        List<Movie> movies = movieService.getVisibleMoviesWithFilters(search, statusEnum, genre, branchId);
        model.addAttribute("movies", movies);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("statusFilter", status != null ? status : "");
        model.addAttribute("genreFilter", genre != null ? genre : "");
        model.addAttribute("branchFilter", branchId);
        model.addAttribute("genreList", genreService.getAllGenres());
        model.addAttribute("branches", cinemaBranchService.getActiveBranches());
        return "movies";
    }

    @GetMapping("/news")
    public String news(@RequestParam(required = false) String q,
                       @RequestParam(required = false) News.NewsType type,
                       @RequestParam(required = false) Long branchId,
                       @RequestParam(defaultValue = "latest") String sort,
                       @RequestParam(defaultValue = "0") int page,
                       HttpSession session,
                       Model model) {
        Page<News> newsPage = newsService.getPublicNews(q, type, branchId, sort, page, 8);
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            model.addAttribute("user", user);
        }
        model.addAttribute("newsList", newsPage.getContent());
        model.addAttribute("currentPage", newsPage.getNumber());
        model.addAttribute("totalPages", newsPage.getTotalPages());
        model.addAttribute("query", q);
        model.addAttribute("typeFilter", type);
        model.addAttribute("branchFilter", branchId);
        model.addAttribute("sortFilter", sort);
        model.addAttribute("newsTypes", News.NewsType.values());
        model.addAttribute("branches", cinemaBranchRepository.findByStatus(com.swp391.team6.cinema.entity.CinemaBranch.BranchStatus.active));
        return "news";
    }

    @GetMapping("/news/{id}")
    public String newsDetail(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            News news = newsService.getPublishedNewsById(id);
            model.addAttribute("news", news);
            User user = (User) session.getAttribute("loggedInUser");
            if (user != null) {
                model.addAttribute("user", user);
            }
            return "news-detail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Tin tức không tồn tại hoặc chưa được xuất bản.");
            return "redirect:/news";
        }
    }

    @GetMapping("/movies/{id}")
    public String movieDetail(@PathVariable Long id, HttpSession session, Model model) {
        Movie movie = movieService.getVisibleMovieById(id);
        List<Showtime> showtimes = showtimeRepository.findByMovieIdOpenAfterWithRoomAndBranch(id, LocalDateTime.now());
        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimes);
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) model.addAttribute("user", user);

        model.addAttribute("review", new Review());
        List<Review> reviews = reviewService.getReviewsByMovieId(id);
        model.addAttribute("reviews", reviews);

        return "movie-detail";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cinemas")
    public String cinemas(@RequestParam(required = false) String keyword,
                          Model model) {
        List<CinemaBranch> branches;
        if (keyword != null && !keyword.trim().isEmpty()) {
            branches = cinemaBranchService.searchBranches(keyword).stream()
                    .filter(b -> b.getStatus() == CinemaBranch.BranchStatus.active)
                    .collect(Collectors.toList());
        } else {
            branches = cinemaBranchService.getActiveBranches();
        }
        model.addAttribute("branches", branches);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "cinema-list";
    }

    @GetMapping("/showtimes")
    public String showtimes(@RequestParam(required = false) Long branchId,
                            @RequestParam(required = false) Long movieId,
                            HttpSession session,
                            Model model) {
        List<Showtime> showtimes = showtimeRepository.findAllOpenFromNow(LocalDateTime.now());

        if (branchId != null) {
            showtimes = showtimes.stream()
                    .filter(s -> s.getRoom().getBranch().getBranchId().equals(branchId))
                    .collect(Collectors.toList());
        }
        if (movieId != null) {
            showtimes = showtimes.stream()
                    .filter(s -> s.getMovie().getMovieId().equals(movieId))
                    .collect(Collectors.toList());
        }

        // Group by date (LocalDate of startTime), preserving order
        Map<LocalDate, List<Showtime>> showtimesByDate = showtimes.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStartTime().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<CinemaBranch> branches = cinemaBranchService.getActiveBranches();
        List<Movie> movies = movieService.getVisibleMoviesByStatus(Movie.MovieStatus.now_showing);

        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) model.addAttribute("user", user);

        model.addAttribute("showtimesByDate", showtimesByDate);
        model.addAttribute("showtimes", showtimes);
        model.addAttribute("branches", branches);
        model.addAttribute("movies", movies);
        model.addAttribute("selectedBranchId", branchId);
        model.addAttribute("selectedMovieId", movieId);
        return "showtimes";
    }
}
