package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.Room;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.service.RoomService;
import com.swp391.team6.cinema.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final SeatService seatService;

    @GetMapping("/rooms/{id}")
    public String roomDetails(@PathVariable Long id, Model model) {

        Room room = roomService.getRoomById(id);

        List<Seat> seats =
                seatService.getSeatsByRoomOrGenerate(id, room.getTotalSeats());

        model.addAttribute("room", room);
        model.addAttribute("seats", seats);

        return "room-details";
    }

    @GetMapping("/rooms")
    public String roomManagement(
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        model.addAttribute(
                "rooms",
                roomService.searchRooms(keyword)
        );
        model.addAttribute("keyword", keyword);
        return "room-list";
    }
}