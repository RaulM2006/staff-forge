package com.staffforge.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectPositionRepository extends JpaRepository<ProjectPosition, Long> {
    List<ProjectPosition> findByProjectId(Long projectId);
}
