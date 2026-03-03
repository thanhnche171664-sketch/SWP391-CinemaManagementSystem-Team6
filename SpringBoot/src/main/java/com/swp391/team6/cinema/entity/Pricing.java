package com.swp391.team6.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "pricing")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long priceId;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private CinemaBranch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type")
    private Seat.SeatType seatType;

    @Column(name = "time_range", length = 50)
    private String timeRange;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
}
