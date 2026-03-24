package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Movie;
import com.swp391.team6.cinema.entity.Room;
import com.swp391.team6.cinema.entity.Showtime;
import com.swp391.team6.cinema.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
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

    public List<Showtime> getAll() {
        return showtimeRepository.findAllByOrderByStartTimeAsc();
    }

    public List<Showtime> getByDate(LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        return showtimeRepository.findByDateRange(start, end);
    }

    public Showtime getByIdWithDetails(Long id) {
        return showtimeRepository.findByIdWithMovieRoomBranch(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu"));
    }

    public void createShowtime(Long movieId, Long roomId, LocalDateTime startTime) {

        Movie movie = movieService.getMovieById(movieId);
        Room room = roomService.getRoomById(roomId);

        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

        List<Showtime> overlaps = showtimeRepository.checkOverlap(
                roomId,
                startTime,
                endTime
        );

        if (!overlaps.isEmpty()) {
            throw new RuntimeException("Phòng đã có suất chiếu trong thời gian này!");
        }

        Showtime s = new Showtime();
        s.setMovie(movie);
        s.setRoom(room);
        s.setStartTime(startTime);
        s.setEndTime(endTime);
        s.setStatus(Showtime.ShowtimeStatus.open);

        showtimeRepository.save(s);
    }

    public List<Showtime> getByDateAndBranch(LocalDate date, Long branchId) {

        LocalDateTime start = date.atStartOfDay();          // 00:00
        LocalDateTime end = date.atTime(23, 59, 59);        // 23:59:59

        return showtimeRepository.findByDateAndBranch(start, end, branchId);
    }

    public List<Showtime> getByBranch(Long branchId) {
        return showtimeRepository.findByBranchId(branchId);
    }

    public void updateShowtime(Long id, Long movieId, Long roomId, LocalDateTime time) {

        Showtime s = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu"));

//        s.setMovie(movieService.getById(movieId));
//        s.setRoom(roomService.getById(roomId));
        s.setStartTime(time);

        showtimeRepository.save(s);
    }
}