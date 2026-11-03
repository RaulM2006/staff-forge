package com.staffforge.allocation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByPositionId(Long positionId);

    List<Allocation> findByUserId(Long userId);

    List<Allocation> findByPositionProjectId(Long projectId);
}
