package com.swp391.team6.cinema.dto;

import com.swp391.team6.cinema.entity.Genre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreDTO {
    private Long genreId;
    private String genreName;
    private String description;
    private Genre.GenreStatus status;
}
