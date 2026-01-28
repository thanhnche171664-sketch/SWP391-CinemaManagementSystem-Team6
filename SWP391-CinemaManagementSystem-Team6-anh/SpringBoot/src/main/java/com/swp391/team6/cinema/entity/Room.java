package com.swp391.team6.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private CinemaBranch branch;

    @Column(name = "room_name", length = 50)
    private String roomName;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoomStatus status = RoomStatus.active;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Seat> seats;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Showtime> showtimes;

    public enum RoomStatus {
        active, inactive
    }
}
