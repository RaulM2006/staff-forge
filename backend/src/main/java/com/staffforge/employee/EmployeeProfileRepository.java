package com.staffforge.employee;

import com.staffforge.project.SeniorityLevel;
import com.staffforge.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {
    Optional<EmployeeProfile> findByUserId(Long userId);

    Optional<EmployeeProfile> findByUser(User user);

    List<EmployeeProfile> findByDepartment(String department);

    List<EmployeeProfile> findBySeniorityLevel(SeniorityLevel seniorityLevel);
}
