package com.staffforge.profile.dto;

import com.staffforge.employee.EmployeeSkill;

import java.time.LocalDateTime;

public class EmployeeSkillResponseDto {

    private Long id;
    private Long skillId;
    private String skillName;
    private String skillCategory;
    private String proficiencyLevel;
    private Integer yearsOfExperience;
    private LocalDateTime createdAt;

    private EmployeeSkillResponseDto() {}

    public static EmployeeSkillResponseDto fromEntity(EmployeeSkill employeeSkill) {
        EmployeeSkillResponseDto dto = new EmployeeSkillResponseDto();
        dto.id = employeeSkill.getId();
        dto.skillId = employeeSkill.getSkill().getId();
        dto.skillName = employeeSkill.getSkill().getName();
        dto.skillCategory = employeeSkill.getSkill().getCategory();
        dto.proficiencyLevel = employeeSkill.getProficiencyLevel().name();
        dto.yearsOfExperience = employeeSkill.getYearsOfExperience();
        dto.createdAt = employeeSkill.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getSkillId() { return skillId; }
    public String getSkillName() { return skillName; }
    public String getSkillCategory() { return skillCategory; }
    public String getProficiencyLevel() { return proficiencyLevel; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
