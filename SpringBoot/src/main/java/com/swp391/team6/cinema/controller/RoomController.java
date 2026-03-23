package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.RoomDTO;
import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.Room;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.repository.SeatRepository;
import com.swp391.team6.cinema.service.CinemaBranchService;
import com.swp391.team6.cinema.service.RoomService;
import com.swp391.team6.cinema.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final SeatService seatService;
    private final CinemaBranchService branchService;
    private final SeatRepository seatRepository;

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
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        int pageSize = 6;

        var roomPage = roomService.searchRoomsPaging(keyword, branchId, page, pageSize);

        List<CinemaBranch> branches = branchService.getAllBranches();

        model.addAttribute("rooms", roomPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roomPage.getTotalPages());

        model.addAttribute("keyword", keyword);
        model.addAttribute("branchId", branchId);

        model.addAttribute("branches", branches);

        return "room-list";
    }

    @GetMapping("/rooms/new")
    public String showCreateRoomForm(Model model) {

        List<CinemaBranch> branches = branchService.getAllBranches();

        model.addAttribute("room", new Room());
        model.addAttribute("branches", branches);

        return "room-create";
    }

    @PostMapping("/rooms/create")
    public String createRoom(Room room, Model model) {

        List<String> errors = new ArrayList<>();

        if (room.getRoomName() == null || room.getRoomName().trim().isEmpty()) {
            errors.add("Tên phòng chiếu không được để trống");
        } else if (room.getRoomName().length() > 255) {
            errors.add("Tên phòng chiếu phải nhỏ hơn 255 kí tự");
        }

        if (room.getTotalSeats() == null) {
            errors.add("Số lượng ghế không được để trống");
        } else {

            if (room.getTotalSeats() <= 0) {
                errors.add("Tổng số lượng ghế không được nhỏ hơn 1");
            }

            if (room.getTotalSeats() > 120) {
                errors.add("Tổng số lượng ghế không được lớn hơn 120");
            }
        }

        if (roomService.existsByRoomNameAndBranch(
                room.getRoomName(),
                room.getBranch().getBranchId()
        )) {
            errors.add("Phòng chiếu đã tồn tại trong rạp bạn chọn");
        }

        if (!errors.isEmpty()) {

            model.addAttribute("errors", errors);
            model.addAttribute("branches", branchService.getAllBranches());

            return "room-create";
        }

        roomService.save(room);

        return "redirect:/rooms";
    }

    @PostMapping("/rooms/{id}/toggle-status")
    public String toggleRoomStatus(@PathVariable Long id) {

        Room room = roomService.getRoomById(id);

        if (room.getStatus() == Room.RoomStatus.active) {
            room.setStatus(Room.RoomStatus.inactive);
        } else {
            room.setStatus(Room.RoomStatus.active);
        }

        roomService.save(room);

        return "redirect:/rooms";
    }

    @PostMapping("/rooms/change-type")
    public String changeSeatType(
            @RequestParam Long seatId,
            @RequestParam Seat.SeatType seatType,
            @RequestParam Long roomId) {

        Seat seat = seatRepository.findById(seatId).orElseThrow();
        seat.setSeatType(seatType);

        seatRepository.save(seat);

        return "redirect:/rooms/" + roomId;
    }

    @PostMapping("/admin/seats/update-type")
    @ResponseBody
    public ResponseEntity<?> updateSeatType(@RequestBody Map<String, Object> req) {

        Long seatId = Long.valueOf(req.get("seatId").toString());
        String type = req.get("type").toString();

        seatService.updateSeatType(seatId, type);

        return ResponseEntity.ok().build();
    }

}