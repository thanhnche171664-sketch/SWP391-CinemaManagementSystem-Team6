package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CinemaBranchService {

    private final CinemaBranchRepository cinemaBranchRepository;

    public List<CinemaBranch> getAllBranches() {
        return cinemaBranchRepository.findAll();
    }

    public void createBranch(CinemaBranch branch) {
        branch.setStatus(CinemaBranch.BranchStatus.active);
        cinemaBranchRepository.save(branch);
    }
}
