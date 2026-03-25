package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.GenreDTO;
import com.swp391.team6.cinema.dto.MovieDTO;
import com.swp391.team6.cinema.entity.Genre;
import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.repository.GenreRepository;
import com.swp391.team6.cinema.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private static final Set<String> ALLOWED_AGE_RATINGS = new HashSet<>(Arrays.asList(
            "G", "PG", "PG-13", "C13", "C16", "C18", "P"
    ));

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie getById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim"));
    }

    @Transactional(readOnly = true)
    public List<Movie> getVisibleMovies() {
        List<Movie> movies = movieRepository.findByIsHiddenFalse();
        movies.forEach(movie -> movie.getGenres().size());
        return movies;
    }

    @Transactional(readOnly = true)
    public List<Movie> getVisibleMoviesWithFilters(String keyword, Movie.MovieStatus status, String genreName, Long branchId) {
        List<Movie> movies = movieRepository.findVisibleMoviesWithFilters(
                (keyword != null && !keyword.isBlank()) ? keyword.trim() : null,
                status,
                (genreName != null && !genreName.isBlank()) ? genreName.trim() : null,
                branchId);
        movies.forEach(movie -> movie.getGenres().size());
        return movies;
    }

    @Transactional(readOnly = true)
    public List<Movie> getVisibleMoviesByStatus(Movie.MovieStatus status) {
        List<Movie> movies = movieRepository.findByStatusAndIsHiddenFalse(status);
        movies.forEach(movie -> movie.getGenres().size());
        return movies;
    }

    @Transactional(readOnly = true)
    public List<Movie> getLatestVisibleMoviesByStatus(Movie.MovieStatus status, int limit) {
        return getVisibleMoviesByStatus(status).stream()
                .sorted(Comparator.comparing(Movie::getReleaseDate,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Movie::getMovieId, Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Movie getVisibleMovieById(Long id) {
        Movie movie = getMovieEntityById(id);
        movie.getGenres().size();
        if (Boolean.TRUE.equals(movie.getIsHidden())) {
            throw new RuntimeException("Movie is hidden with id: " + id);
        }
        return movie;
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getAllMovies(boolean includeHidden) {
        List<Movie> movies = includeHidden ? movieRepository.findAll() : movieRepository.findByIsHiddenFalse();
        return movies.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MovieDTO> getMoviesPage(boolean includeHidden, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "movieId"));
        Page<Movie> moviePage = includeHidden
                ? movieRepository.findAll(pageRequest)
                : movieRepository.findByIsHiddenFalse(pageRequest);
        return moviePage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovieDTO> getMoviesPageWithFilters(int page, int size, String keyword,
                                                   Movie.MovieStatus status, Boolean hidden) {
        String normalizedKeyword = (keyword != null && !keyword.isBlank())
                ? keyword.trim()
                : null;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "movieId"));
        Page<Movie> moviePage = movieRepository.findAdminMoviesWithFilters(
                normalizedKeyword,
                status,
                hidden,
                pageRequest);
        return moviePage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovieDTO> getBranchMoviesPageWithFilters(Long branchId, int page, int size, String keyword,
                                                         Movie.MovieStatus status, Boolean hidden) {
        if (branchId == null) {
            throw new IllegalArgumentException("Branch không hợp lệ.");
        }
        String normalizedKeyword = (keyword != null && !keyword.isBlank())
                ? keyword.trim()
                : null;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "movieId"));
        Page<Movie> moviePage = movieRepository.findBranchMoviesForManagement(
                branchId,
                normalizedKeyword,
                status,
                hidden,
                pageRequest);
        return moviePage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public MovieDTO getMovieById(Long id, boolean includeHidden) {
        Movie movie = getMovieEntityById(id);
        if (!includeHidden && Boolean.TRUE.equals(movie.getIsHidden())) {
            throw new RuntimeException("Movie is hidden with id: " + id);
        }
        return convertToDTO(movie);
    }

    public Movie getMovieById(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim với id = " + movieId));
    }

    @Transactional
    public MovieDTO createMovie(MovieDTO movieDTO) {
        validateMovieDTO(movieDTO, null, true);
        Movie movie = convertToEntity(movieDTO);
        movie.setGenres(resolveGenres(movieDTO.getGenreIds()));
        // Ensure new entity is always inserted
        movie.setMovieId(null);
        Movie savedMovie = movieRepository.save(movie);
        return convertToDTO(savedMovie);
    }

    @Transactional
    public MovieDTO updateMovie(Long id, MovieDTO movieDTO) {
        Movie existingMovie = getMovieEntityById(id);

        if (movieDTO.getTitle() != null) {
            existingMovie.setTitle(normalizeText(movieDTO.getTitle()));
        }
        if (movieDTO.getDescription() != null) {
            String description = normalizeText(movieDTO.getDescription());
            existingMovie.setDescription(description.isEmpty() ? null : description);
        }
        if (movieDTO.getDuration() != null) {
            existingMovie.setDuration(movieDTO.getDuration());
        }
        if (movieDTO.getGenreIds() != null) {
            existingMovie.setGenres(resolveGenres(movieDTO.getGenreIds()));
        }
        if (movieDTO.getAgeRating() != null) {
            existingMovie.setAgeRating(normalizeText(movieDTO.getAgeRating()));
        }
        if (movieDTO.getPosterUrl() != null) {
            existingMovie.setPosterUrl(normalizeText(movieDTO.getPosterUrl()));
        }
        if (movieDTO.getTrailerUrl() != null) {
            existingMovie.setTrailerUrl(normalizeText(movieDTO.getTrailerUrl()));
        }
        if (movieDTO.getReleaseDate() != null) {
            existingMovie.setReleaseDate(movieDTO.getReleaseDate());
        }
        if (movieDTO.getStatus() != null) {
            existingMovie.setStatus(movieDTO.getStatus());
        }
        if (movieDTO.getHidden() != null) {
            existingMovie.setIsHidden(movieDTO.getHidden());
        }

        validateMovieEntity(existingMovie, id);

        Movie updatedMovie = movieRepository.save(existingMovie);
        return convertToDTO(updatedMovie);
    }

    @Transactional
    public MovieDTO updateVisibility(Long id, boolean hidden) {
        Movie movie = getMovieEntityById(id);
        movie.setIsHidden(hidden);
        return convertToDTO(movieRepository.save(movie));
    }

    private Movie getMovieEntityById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
    }

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setMovieId(movie.getMovieId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDuration(movie.getDuration());
        dto.setAgeRating(movie.getAgeRating());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setStatus(movie.getStatus());
        dto.setHidden(movie.getIsHidden());
        dto.setGenreIds(movie.getGenres()
                .stream()
                .map(Genre::getGenreId)
                .collect(Collectors.toList()));
        dto.setGenreNames(movie.getGenreNames());
        return dto;
    }

    private Movie convertToEntity(MovieDTO dto) {
        Movie movie = new Movie();
        movie.setTitle(normalizeText(dto.getTitle()));
        String description = normalizeText(dto.getDescription());
        movie.setDescription(description.isEmpty() ? null : description);
        movie.setDuration(dto.getDuration());
        movie.setAgeRating(normalizeText(dto.getAgeRating()));
        movie.setPosterUrl(normalizeText(dto.getPosterUrl()));
        movie.setTrailerUrl(normalizeText(dto.getTrailerUrl()));
        movie.setReleaseDate(dto.getReleaseDate());
        if (dto.getStatus() != null) {
            movie.setStatus(dto.getStatus());
        }
        if (dto.getHidden() != null) {
            movie.setIsHidden(dto.getHidden());
        }
        return movie;
    }

    private List<Genre> resolveGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new ArrayList<>();
        }
        return genreRepository.findAllById(genreIds);
    }

    private void validateMovieDTO(MovieDTO dto, Long currentMovieId, boolean isCreate) {
        String title = normalizeText(dto.getTitle());
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Tên phim không được để trống.");
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException("Tên phim tối đa 255 ký tự.");
        }
        Optional<Movie> duplicate = movieRepository.findByTitleIgnoreCase(title);
        if (duplicate.isPresent() && (currentMovieId == null ||
                !duplicate.get().getMovieId().equals(currentMovieId))) {
            throw new IllegalArgumentException("Tên phim đã tồn tại.");
        }

        String description = normalizeText(dto.getDescription());
        if (!description.isEmpty() && description.length() > 2000) {
            throw new IllegalArgumentException("Mô tả tối đa 2000 ký tự.");
        }

        Integer duration = dto.getDuration();
        if (duration == null) {
            throw new IllegalArgumentException("Thời lượng phim là bắt buộc.");
        }
        if (duration < 30 || duration > 300) {
            throw new IllegalArgumentException("Thời lượng phim phải từ 30 đến 300 phút.");
        }

        if (dto.getGenreIds() == null || dto.getGenreIds().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một thể loại.");
        }

        String ageRating = normalizeText(dto.getAgeRating());
        if (ageRating.isEmpty()) {
            throw new IllegalArgumentException("Độ tuổi là bắt buộc.");
        }
        if (!ALLOWED_AGE_RATINGS.contains(ageRating.toUpperCase())) {
            throw new IllegalArgumentException("Độ tuổi không hợp lệ. Ví dụ: G, PG, PG-13, C13, C16, C18.");
        }

        LocalDate releaseDate = dto.getReleaseDate();
        if (releaseDate == null) {
            throw new IllegalArgumentException("Ngày phát hành là bắt buộc.");
        }
        if (isCreate && releaseDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày phát hành không được nhỏ hơn ngày hiện tại.");
        }
    }

    private void validateMovieEntity(Movie movie, Long currentMovieId) {
        MovieDTO dto = new MovieDTO();
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDuration(movie.getDuration());
        dto.setGenreIds(movie.getGenres().stream()
                .map(Genre::getGenreId)
                .collect(Collectors.toList()));
        dto.setAgeRating(movie.getAgeRating());
        dto.setReleaseDate(movie.getReleaseDate());
        validateMovieDTO(dto, currentMovieId, false);
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAllByOrderByGenreNameAsc()
                .stream()
                .map(this::convertGenresToDTO)
                .collect(Collectors.toList());
    }

    private GenreDTO convertGenresToDTO(Genre genre) {
        GenreDTO dto = new GenreDTO();
        dto.setGenreId(genre.getGenreId());
        dto.setGenreName(genre.getGenreName());
        dto.setDescription(genre.getDescription());
        dto.setStatus(genre.getStatus());
        return dto;
    }

    /** Get visible movies for a specific branch (via branch_movies table) */
    @Transactional(readOnly = true)
    public List<MovieDTO> getMoviesByBranchId(Long branchId) {
        List<Movie> movies = movieRepository.findMoviesByBranchId(branchId);
        movies.forEach(movie -> movie.getGenres().size());
        return movies.stream()
                .filter(m -> !Boolean.TRUE.equals(m.getIsHidden()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
