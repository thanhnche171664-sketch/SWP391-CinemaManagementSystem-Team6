package com.swp391.team6.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffDTO {
    private Long user_id;
    private String full_name;
    private String email;
    private String phone;
    private String role;
    private String status;
    private LocalDateTime created_at;

    private Long branch_id;
    private String branch_name;
    private String password;
}