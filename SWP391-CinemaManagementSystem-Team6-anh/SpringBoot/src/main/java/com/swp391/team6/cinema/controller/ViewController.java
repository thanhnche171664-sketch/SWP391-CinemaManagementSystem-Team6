package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.service.MovieService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    
    @GetMapping("/home")
    public String customerHome(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/auth/login";
        }
        
        List<Movie> movies = movieService.getVisibleMoviesByStatus(Movie.MovieStatus.now_showing);
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
        
        model.addAttribute("user", user);
        return "redirect:/admin/customers"; // Hoặc trang dashboard admin
    }
    
    @GetMapping("/manager")
    public String managerDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || (user.getRole() != User.UserRole.MANAGER && user.getRole() != User.UserRole.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/auth/login";
        }
        
        model.addAttribute("user", user);
        return "redirect:/admin/branch"; // Trang quản lý branch
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
        return "redirect:/admin/staff"; // Trang quản lý staff
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
