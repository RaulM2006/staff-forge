package com.staffforge.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
    List<EmployeeSkill> findByEmployeeProfileId(Long employeeProfileId);

    List<EmployeeSkill> findBySkillId(Long skillId);

    Optional<EmployeeSkill> findByEmployeeProfileIdAndSkillId(Long employeeProfileId, Long skillId);

    boolean existsByEmployeeProfileIdAndSkillId(Long employeeProfileId, Long skillId);
}
