package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.GenreDTO;
import com.swp391.team6.cinema.entity.Genre;
import com.swp391.team6.cinema.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAllByOrderByGenreNameAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public GenreDTO saveGenre(GenreDTO dto) {
        String normalizedName = normalizeName(dto.getGenreName());
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Tên thể loại không được để trống.");
        }

        Optional<Genre> duplicate = genreRepository.findByGenreNameIgnoreCase(normalizedName);
        if (duplicate.isPresent() && (dto.getGenreId() == null ||
                !duplicate.get().getGenreId().equals(dto.getGenreId()))) {
            throw new IllegalArgumentException("Tên thể loại đã tồn tại.");
        }

        Genre genre = (dto.getGenreId() == null)
                ? new Genre()
                : genreRepository.findById(dto.getGenreId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại."));

        genre.setGenreName(normalizedName);
        genre.setDescription(dto.getDescription() == null ? null : dto.getDescription().trim());
        if (dto.getStatus() != null) {
            genre.setStatus(dto.getStatus());
        }

        return convertToDTO(genreRepository.save(genre));
    }

    @Transactional
    public void toggleStatus(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại."));
        Genre.GenreStatus next =
                genre.getStatus() == Genre.GenreStatus.active ? Genre.GenreStatus.inactive : Genre.GenreStatus.active;
        genre.setStatus(next);
        genreRepository.save(genre);
    }

    private GenreDTO convertToDTO(Genre genre) {
        GenreDTO dto = new GenreDTO();
        dto.setGenreId(genre.getGenreId());
        dto.setGenreName(genre.getGenreName());
        dto.setDescription(genre.getDescription());
        dto.setStatus(genre.getStatus());
        return dto;
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }
}
