package com.swp391.team6.cinema.service.impl;

import com.swp391.team6.cinema.dto.booking.BookingSummaryDto;
import com.swp391.team6.cinema.dto.booking.CreateBookingRequest;
import com.swp391.team6.cinema.entity.*;
import com.swp391.team6.cinema.repository.*;
import com.swp391.team6.cinema.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final PricingRepository pricingRepository;

    @Override
    @Transactional
    public Booking createPendingBooking(User user, CreateBookingRequest request) {
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        if (showtime.getStatus() != Showtime.ShowtimeStatus.open) {
            throw new RuntimeException("Showtime is not open for booking");
        }

        List<Long> seatIds = request.getSeatIds();
        if (seatIds == null || seatIds.isEmpty()) {
            throw new RuntimeException("No seats selected");
        }

        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Some selected seats do not exist");
        }

        Long roomId = showtime.getRoom().getRoomId();
        boolean invalidRoom = seats.stream()
                .anyMatch(seat -> seat.getRoom() == null || !roomId.equals(seat.getRoom().getRoomId()));
        if (invalidRoom) {
            throw new RuntimeException("Selected seats do not belong to the showtime room");
        }

        List<Booking.BookingStatus> lockStatuses = Arrays.asList(
                Booking.BookingStatus.pending,
                Booking.BookingStatus.paid
        );

        List<BookingSeat> existing = bookingSeatRepository
                .findBySeatSeatIdInAndBookingShowtimeShowtimeIdAndBookingStatusIn(
                        seatIds,
                        showtime.getShowtimeId(),
                        lockStatuses
                );

        if (!existing.isEmpty()) {
            throw new RuntimeException("One or more selected seats have already been booked");
        }

        BigDecimal totalAmount = seats.stream()
                .map(seat -> calculateSeatPrice(showtime, seat))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setBookingType(Booking.BookingType.online);
        booking.setStatus(Booking.BookingStatus.pending);
        booking.setTotalAmount(totalAmount);

        List<BookingSeat> bookingSeats = new ArrayList<>();
        for (Seat seat : seats) {
            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(booking);
            bookingSeat.setSeat(seat);
            bookingSeats.add(bookingSeat);
        }
        booking.setBookingSeats(bookingSeats);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void markBookingPaid(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(Booking.BookingStatus.paid);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void markBookingFailedOrCancelled(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(Booking.BookingStatus.cancelled);
        bookingRepository.save(booking);
    }

    @Override
    public Optional<Booking> findByIdForUser(Long bookingId, User user) {
        return bookingRepository.findById(bookingId)
                .filter(booking -> booking.getUser() != null
                        && booking.getUser().getUserId() != null
                        && booking.getUser().getUserId().equals(user.getUserId()));
    }

    @Override
    public List<BookingSummaryDto> getBookingsForUser(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("User is required");
        }
        List<Booking> bookings = bookingRepository.findByUserUserId(user.getUserId());
        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getBookingTime).reversed())
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingSummaryDto toSummaryDto(Booking booking) {
        String movieTitle = booking.getShowtime().getMovie().getTitle();
        Room room = booking.getShowtime().getRoom();
        String roomName = room.getRoomName();
        String branchName = room.getBranch() != null ? room.getBranch().getBranchName() : "";

        List<String> seatLabels = booking.getBookingSeats() == null
                ? List.of()
                : booking.getBookingSeats().stream()
                .map(bs -> {
                    Seat seat = bs.getSeat();
                    if (seat == null) {
                        return "";
                    }
                    return seat.getSeatRow() + seat.getSeatNumber();
                })
                .filter(label -> !label.isBlank())
                .collect(Collectors.toList());

        return new BookingSummaryDto(
                booking.getBookingId(),
                movieTitle,
                branchName,
                roomName,
                booking.getShowtime().getStartTime(),
                seatLabels,
                booking.getTotalAmount(),
                booking.getStatus()
        );
    }

    private BigDecimal calculateSeatPrice(Showtime showtime, Seat seat) {
        Long branchId = showtime.getRoom().getBranch().getBranchId();
        return pricingRepository.findFirstByBranchBranchIdAndSeatType(branchId, seat.getSeatType())
                .map(Pricing::getPrice)
                .orElseThrow(() -> new RuntimeException("Pricing not configured for seat type " +
                        seat.getSeatType() + " in this branch"));
    }
}

