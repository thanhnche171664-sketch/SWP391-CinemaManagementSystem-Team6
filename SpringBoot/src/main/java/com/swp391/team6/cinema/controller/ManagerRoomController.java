package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.entity.Room;
import com.swp391.team6.cinema.entity.Seat;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.SeatRepository;
import com.swp391.team6.cinema.service.CinemaBranchService;
import com.swp391.team6.cinema.service.RoomService;
import com.swp391.team6.cinema.service.SeatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager/rooms")
@RequiredArgsConstructor
public class ManagerRoomController {

    private final RoomService roomService;
    private final SeatService seatService;
    private final CinemaBranchService branchService;
    private final SeatRepository seatRepository;

    @GetMapping("/{id}")
    public String roomDetails(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Room room = roomService.getRoomById(id);
        if (!belongsToBranch(room, user.getBranchId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập phòng chiếu này.");
            return "redirect:/manager/rooms";
        }

        List<Seat> seats = seatService.getSeatsByRoomOrGenerate(id, room.getTotalSeats());

        model.addAttribute("room", room);
        model.addAttribute("seats", seats);
        model.addAttribute("roomBasePath", "/manager/rooms");
        model.addAttribute("seatUpdatePath", "/manager/rooms/seats/update-type");
        model.addAttribute("isManager", true);

        return "room-details";
    }

    @GetMapping
    public String roomManagement(@RequestParam(required = false) String keyword,
                                 @RequestParam(defaultValue = "0") int page,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        int pageSize = 6;
        Long branchId = user.getBranchId();

        var roomPage = roomService.searchRoomsPaging(keyword, branchId, page, pageSize);
        List<CinemaBranch> branches = List.of(branchService.getBranchById(branchId));

        model.addAttribute("rooms", roomPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roomPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("branchId", branchId);
        model.addAttribute("branches", branches);
        model.addAttribute("roomBasePath", "/manager/rooms");
        model.addAttribute("branchBasePath", "/branches");
        model.addAttribute("pricingBasePath", "/manager/pricing");
        model.addAttribute("isManager", true);

        return "room-list";
    }

    @GetMapping("/new")
    public String showCreateRoomForm(HttpSession session,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        CinemaBranch branch = branchService.getBranchById(user.getBranchId());
        model.addAttribute("room", new Room());
        model.addAttribute("branches", List.of(branch));
        model.addAttribute("selectedBranchId", user.getBranchId());
        model.addAttribute("roomBasePath", "/manager/rooms");
        model.addAttribute("branchBasePath", "/branches");
        model.addAttribute("pricingBasePath", "/manager/pricing");
        model.addAttribute("isManager", true);

        return "room-create";
    }

    @PostMapping("/create")
    public String createRoom(Room room,
                             HttpSession session,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        CinemaBranch branch = branchService.getBranchById(user.getBranchId());
        room.setBranch(branch);

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
            errors.add("Phòng chiếu đã tồn tại trong rạp bạn quản lý");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("branches", List.of(branch));
            model.addAttribute("selectedBranchId", user.getBranchId());
            model.addAttribute("roomBasePath", "/manager/rooms");
            model.addAttribute("branchBasePath", "/branches");
            model.addAttribute("pricingBasePath", "/manager/pricing");
            model.addAttribute("isManager", true);
            return "room-create";
        }

        roomService.save(room);
        return "redirect:/manager/rooms";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleRoomStatus(@PathVariable Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User user = requireManager(session, redirectAttributes);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Room room = roomService.getRoomById(id);
        if (!belongsToBranch(room, user.getBranchId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thao tác phòng chiếu này.");
            return "redirect:/manager/rooms";
        }

        if (room.getStatus() == Room.RoomStatus.active) {
            room.setStatus(Room.RoomStatus.inactive);
        } else {
            room.setStatus(Room.RoomStatus.active);
        }

        roomService.save(room);
        return "redirect:/manager/rooms";
    }

    @PostMapping("/seats/update-type")
    @ResponseBody
    public ResponseEntity<?> updateSeatType(@RequestBody Map<String, Object> req,
                                            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.MANAGER || user.getBranchId() == null) {
            return ResponseEntity.status(403).build();
        }

        Long seatId = Long.valueOf(req.get("seatId").toString());
        String type = req.get("type").toString();

        Seat seat = seatRepository.findById(seatId).orElseThrow();
        if (seat.getRoom() == null || seat.getRoom().getBranch() == null
                || !user.getBranchId().equals(seat.getRoom().getBranch().getBranchId())) {
            return ResponseEntity.status(403).build();
        }

        seatService.updateSeatType(seatId, type);
        return ResponseEntity.ok().build();
    }

    private User requireManager(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != User.UserRole.MANAGER) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return null;
        }
        if (user.getBranchId() == null) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản quản lý chưa có chi nhánh.");
            return null;
        }
        return user;
    }

    private boolean belongsToBranch(Room room, Long branchId) {
        return room.getBranch() != null
                && room.getBranch().getBranchId() != null
                && room.getBranch().getBranchId().equals(branchId);
    }
}
