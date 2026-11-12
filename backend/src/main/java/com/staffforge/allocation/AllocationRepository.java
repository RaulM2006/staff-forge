package com.staffforge.allocation;

import com.staffforge.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByPositionId(Long positionId);

    @Query("SELECT a FROM Allocation a WHERE a.user.id = :userId")
    List<Allocation> findByUserId(@Param("userId") Long userId);

    List<Allocation> findByUser(User user);

    List<Allocation> findByPositionProjectId(Long projectId);
}
