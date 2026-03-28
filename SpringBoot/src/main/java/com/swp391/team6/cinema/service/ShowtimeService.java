package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.entity.Room;
import com.swp391.team6.cinema.entity.Showtime;
import com.swp391.team6.cinema.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieService movieService;
    private final RoomService roomService;

    public Page<Showtime> getAll(Pageable pageable) {

        Page<Showtime> page = showtimeRepository.findAll(pageable);

        LocalDateTime now = LocalDateTime.now();

        page.getContent().forEach(s -> {
            if (s.getEndTime() != null && s.getEndTime().isBefore(now)
                    && s.getStatus() != Showtime.ShowtimeStatus.closed) {

                s.setStatus(Showtime.ShowtimeStatus.closed);
            }
        });

        return page;
    }

    @Scheduled(fixedRate = 60000) // mỗi 1 phút
    public void updateShowtimeStatus() {

        List<Showtime> list = showtimeRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        list.forEach(s -> {
            if (s.getStatus() != Showtime.ShowtimeStatus.cancelled && s.getEndTime() != null && s.getEndTime().isBefore(now)) {
                s.setStatus(Showtime.ShowtimeStatus.closed);
            }
        });

        showtimeRepository.saveAll(list);
    }

    public Page<Showtime> getByDate(LocalDate date, Pageable pageable) {
        return showtimeRepository.findByStartTimeBetween(
                date.atStartOfDay(),
                date.atTime(23,59,59),
                pageable
        );
    }

    public Page<Showtime> getByDateAndBranch(LocalDate date, Long branchId, Pageable pageable) {
        return showtimeRepository.findByRoom_Branch_BranchIdAndStartTimeBetween(
                branchId,
                date.atStartOfDay(),
                date.atTime(23,59,59),
                pageable
        );
    }

    public Showtime getByIdWithDetails(Long id) {
        return showtimeRepository.findByIdWithMovieRoomBranch(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu"));
    }

    public Page<Showtime> getByBranch(Long branchId, Pageable pageable) {
        return showtimeRepository.findByRoom_Branch_BranchId(branchId, pageable);
    }


    public Showtime getById(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy showtime"));
    }

    public void updateStatus(Long id, String status) {
        Showtime s = getById(id);

        s.setStatus(Showtime.ShowtimeStatus.valueOf(status));

        showtimeRepository.save(s);
    }

    public void updateShowtime(Long id, Long movieId, Long roomId, LocalDateTime time) {

        Showtime s = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu"));

//        s.setMovie(movieService.getById(movieId));
//        s.setRoom(roomService.getById(roomId));
        s.setStartTime(time);

        showtimeRepository.save(s);
    }

    public void createShowtime(Long movieId, Long roomId, LocalDateTime time) {

        Movie movie = movieService.getById(movieId);
        Room room = roomService.getById(roomId);

        if (movie.getReleaseDate() != null && !time.toLocalDate().isAfter(movie.getReleaseDate())) {
            throw new RuntimeException("Ngày chiếu phải lớn hơn ngày phát hành của phim.");
        }

        LocalDateTime endTime = time.plusMinutes(movie.getDuration());

        if (movie.getStatus() != Movie.MovieStatus.now_showing) {
            throw new RuntimeException("Chỉ được tạo suất chiếu cho phim đang chiếu");
        }
        //CHECK TRÙNG
        List<Showtime> overlap = showtimeRepository
                .findByRoom_RoomId(roomId)
                .stream()
                .filter(s ->
                        s.getStartTime().isBefore(endTime) &&
                                time.isBefore(s.getEndTime())
                )
                .toList();

        if (!overlap.isEmpty()) {
            throw new RuntimeException("Trùng lịch chiếu trong phòng này");
        }

        Showtime s = new Showtime();
        s.setMovie(movie);
        s.setRoom(room);
        s.setStartTime(time);
        s.setEndTime(endTime);

        showtimeRepository.save(s);
    }


}