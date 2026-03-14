package com.swp391.team6.cinema.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movie_genres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;
}