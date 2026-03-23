package com.swp391.team6.cinema.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    @JsonIgnore
    private CinemaBranch branch;
    @Column(name = "room_name", length = 50)
    private String roomName;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoomStatus status = RoomStatus.active;


    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Seat> seats;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Showtime> showtimes;

    public enum RoomStatus {
        active, inactive
    }
}
