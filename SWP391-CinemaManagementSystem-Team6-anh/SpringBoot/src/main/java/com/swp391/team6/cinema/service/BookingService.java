package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.dto.booking.BookingSummaryDto;
import com.swp391.team6.cinema.dto.booking.CreateBookingRequest;
import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.User;

import java.util.List;
import java.util.Optional;

public interface BookingService {

    Booking createPendingBooking(User user, CreateBookingRequest request);

    void markBookingPaid(Long bookingId);

    void markBookingFailedOrCancelled(Long bookingId);

    Optional<Booking> findByIdForUser(Long bookingId, User user);

    List<BookingSummaryDto> getBookingsForUser(User user);

    BookingSummaryDto toSummaryDto(Booking booking);
}
