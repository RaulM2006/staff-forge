package com.staffforge.profile.dto;

import com.staffforge.employee.ProficiencyLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddSkillRequestDto {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotNull(message = "Proficiency level is required")
    private ProficiencyLevel proficiencyLevel;

    @Min(value = 0, message = "Years of experience must be a non-negative number")
    private Integer yearsOfExperience = 0;

    public Long getSkillId() { return skillId; }
    public ProficiencyLevel getProficiencyLevel() { return proficiencyLevel; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
}
