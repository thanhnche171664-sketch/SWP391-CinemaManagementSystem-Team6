package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.BranchMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchMovieRepository extends JpaRepository<BranchMovie, Long> {
    
    List<BranchMovie> findByBranchBranchId(Long branchId);
    
    List<BranchMovie> findByMovieMovieId(Long movieId);
}
