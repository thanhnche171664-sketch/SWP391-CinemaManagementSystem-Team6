package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.Room;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.repository.BookingRepository;
import com.swp391.team6.cinema.repository.MovieRepository;
import com.swp391.team6.cinema.repository.RoomRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final BookingRepository bookingRepository;
    private final MovieRepository movieRepository;
    private final CinemaBranchRepository cinemaBranchRepository;
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public AdminDashboardData buildDashboard() {
        List<Booking> bookings = bookingRepository.findAllWithDetails();
        List<Booking> paidBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.paid)
                .toList();

        BigDecimal totalRevenue = paidBookings.stream()
                .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTickets = paidBookings.stream()
                .mapToLong(b -> b.getBookingSeats() != null ? b.getBookingSeats().size() : 0)
                .sum();

        long totalMovies = movieRepository.count();
        long totalBookings = bookings.size();

        List<MonthlyRevenuePoint> monthlyRevenue = buildLastSixMonthsRevenue(paidBookings);
        List<TopMoviePoint> topMovies = buildTopMovies(paidBookings);
        List<BranchOverviewPoint> branches = buildBranchOverview();

        BigDecimal currentMonthRevenue = calcMonthRevenue(paidBookings, YearMonth.now());
        BigDecimal previousMonthRevenue = calcMonthRevenue(paidBookings, YearMonth.now().minusMonths(1));
        double revenueTrend = calcTrendPercent(currentMonthRevenue, previousMonthRevenue);

        long currentMonthTickets = calcMonthTickets(paidBookings, YearMonth.now());
        long previousMonthTickets = calcMonthTickets(paidBookings, YearMonth.now().minusMonths(1));
        double ticketTrend = calcTrendPercent(BigDecimal.valueOf(currentMonthTickets), BigDecimal.valueOf(previousMonthTickets));

        return new AdminDashboardData(
                totalRevenue,
                totalTickets,
                totalMovies,
                totalBookings,
                revenueTrend,
                ticketTrend,
                monthlyRevenue,
                topMovies,
                branches
        );
    }

    private List<MonthlyRevenuePoint> buildLastSixMonthsRevenue(List<Booking> paidBookings) {
        Map<YearMonth, BigDecimal> monthMap = new LinkedHashMap<>();
        YearMonth now = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            monthMap.put(now.minusMonths(i), BigDecimal.ZERO);
        }

        for (Booking booking : paidBookings) {
            if (booking.getBookingTime() == null) continue;
            YearMonth key = YearMonth.from(booking.getBookingTime());
            if (monthMap.containsKey(key)) {
                monthMap.put(key, monthMap.get(key).add(booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO));
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        BigDecimal maxRevenue = monthMap.values().stream()
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        List<MonthlyRevenuePoint> result = new ArrayList<>();
        monthMap.forEach((month, revenue) -> {
            double percent = maxRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? revenue.multiply(BigDecimal.valueOf(100))
                            .divide(maxRevenue, 2, java.math.RoundingMode.HALF_UP)
                            .doubleValue()
                    : 0;
            result.add(new MonthlyRevenuePoint(month.format(formatter), revenue, percent));
        });
        return result;
    }

    private List<TopMoviePoint> buildTopMovies(List<Booking> paidBookings) {
        Map<String, BigDecimal> movieRevenue = new LinkedHashMap<>();
        for (Booking booking : paidBookings) {
            String title = booking.getShowtime() != null && booking.getShowtime().getMovie() != null
                    ? booking.getShowtime().getMovie().getTitle()
                    : "Unknown";
            BigDecimal amount = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
            movieRevenue.put(title, movieRevenue.getOrDefault(title, BigDecimal.ZERO).add(amount));
        }

        return movieRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(e -> new TopMoviePoint(e.getKey(), e.getValue()))
                .toList();
    }

    private BigDecimal calcMonthRevenue(List<Booking> paidBookings, YearMonth ym) {
        return paidBookings.stream()
                .filter(b -> b.getBookingTime() != null && YearMonth.from(b.getBookingTime()).equals(ym))
                .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long calcMonthTickets(List<Booking> paidBookings, YearMonth ym) {
        return paidBookings.stream()
                .filter(b -> b.getBookingTime() != null && YearMonth.from(b.getBookingTime()).equals(ym))
                .mapToLong(b -> b.getBookingSeats() != null ? b.getBookingSeats().size() : 0)
                .sum();
    }

    private double calcTrendPercent(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) <= 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }

    private List<BranchOverviewPoint> buildBranchOverview() {
        List<CinemaBranch> branches = cinemaBranchRepository.findAll().stream()
                .sorted(Comparator.comparing(CinemaBranch::getBranchName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return branches.stream()
                .map(branch -> {
                    List<Room> rooms = roomRepository.findByBranchBranchId(branch.getBranchId());
                    int totalRooms = rooms.size();
                    int totalSeats = rooms.stream()
                            .map(Room::getTotalSeats)
                            .filter(v -> v != null)
                            .reduce(0, Integer::sum);
                    return new BranchOverviewPoint(
                            branch.getBranchName(),
                            branch.getCity(),
                            branch.getAddress(),
                            branch.getStatus() != null ? branch.getStatus().name() : "unknown",
                            totalRooms,
                            totalSeats
                    );
                })
                .collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    public static class AdminDashboardData {
        private BigDecimal totalRevenue;
        private long totalTickets;
        private long totalMovies;
        private long totalBookings;
        private double revenueTrend;
        private double ticketTrend;
        private List<MonthlyRevenuePoint> monthlyRevenue;
        private List<TopMoviePoint> topMovies;
        private List<BranchOverviewPoint> branches;
    }

    @Data
    @AllArgsConstructor
    public static class MonthlyRevenuePoint {
        private String label;
        private BigDecimal value;
        private double percent;
    }

    @Data
    @AllArgsConstructor
    public static class TopMoviePoint {
        private String title;
        private BigDecimal value;
    }

    @Data
    @AllArgsConstructor
    public static class BranchOverviewPoint {
        private String branchName;
        private String city;
        private String address;
        private String status;
        private int totalRooms;
        private int totalSeats;
    }
}
