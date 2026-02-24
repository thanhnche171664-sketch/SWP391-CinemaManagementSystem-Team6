package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.CinemaBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaBranchRepository extends JpaRepository<CinemaBranch, Long> {
    
    List<CinemaBranch> findByCity(String city);
    
    List<CinemaBranch> findByStatus(CinemaBranch.BranchStatus status);

    List<CinemaBranch> findByBranchNameContainingIgnoreCaseOrCityContainingIgnoreCase(
            String branchName,
            String city
    );
}
