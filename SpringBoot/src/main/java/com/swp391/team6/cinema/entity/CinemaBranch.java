package com.swp391.team6.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "cinema_branches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CinemaBranch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "branch_name", length = 100)
    private String branchName;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "address", length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BranchStatus status = BranchStatus.active;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL)
    private List<Room> rooms;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL)
    private List<BranchMovie> branchMovies;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL)
    private List<Pricing> pricings;

    public enum BranchStatus {
        active, inactive
    }
}
