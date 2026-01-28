package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.CinemaBranch;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CinemaBranchService {

    private final CinemaBranchRepository cinemaBranchRepository;

    public List<CinemaBranch> getAllBranches() {
        return cinemaBranchRepository.findAll();
    }

    public CinemaBranch getBranchById(Long id) {
        return cinemaBranchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
    }

    public void createBranch(CinemaBranch branch) {
        branch.setStatus(CinemaBranch.BranchStatus.active);
        cinemaBranchRepository.save(branch);
    }

    public CinemaBranch updateBranch(Long id, CinemaBranch updated) {
        CinemaBranch branch = getBranchById(id);

        branch.setBranchName(updated.getBranchName());
        branch.setCity(updated.getCity());
        branch.setAddress(updated.getAddress());

        return cinemaBranchRepository.save(branch);
    }

    public void save(CinemaBranch branch) {
        cinemaBranchRepository.save(branch);
    }

    public void deleteById(Long id) {
        cinemaBranchRepository.deleteById(id);
    }

    public List<CinemaBranch> searchBranches(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return cinemaBranchRepository.findAll();
        }
        return cinemaBranchRepository
                .findByBranchNameContainingIgnoreCaseOrCityContainingIgnoreCase(
                        keyword, keyword
                );
    }
}
