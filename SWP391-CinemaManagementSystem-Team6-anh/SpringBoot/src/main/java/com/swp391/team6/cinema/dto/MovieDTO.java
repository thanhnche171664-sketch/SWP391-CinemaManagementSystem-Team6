package com.swp391.team6.cinema.dto;

import com.swp391.team6.cinema.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Long movieId;
    private String title;
    private Integer duration;
    private String genre;
    private String ageRating;
    private String description;
    private String posterUrl;
    private String trailerUrl;
    private Movie.MovieStatus status;
    private Boolean hidden;
}
