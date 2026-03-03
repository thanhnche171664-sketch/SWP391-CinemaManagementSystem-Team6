package com.swp391.team6.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"branchMovies", "showtimes", "reviews", "genres"})
@ToString(exclude = {"branchMovies", "showtimes", "reviews", "genres"})
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "title", length = 150)
    private String title;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "age_rating", length = 10)
    private String ageRating;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "poster_url", length = 255)
    private String posterUrl;

    @Column(name = "trailer_url", length = 255)
    private String trailerUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MovieStatus status = MovieStatus.upcoming;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<BranchMovie> branchMovies;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Showtime> showtimes;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @ManyToMany
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres = new ArrayList<>();

    @Transient
    public String getGenreNames() {
        if (genres == null || genres.isEmpty()) {
            return "";
        }
        return genres.stream()
                .map(Genre::getGenreName)
                .collect(Collectors.joining(", "));
    }

    public enum MovieStatus {
        upcoming, now_showing, stopped
    }
}
