package com.staffforge.profile.dto;

import com.staffforge.project.SeniorityLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequestDto {

    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @NotNull(message = "Seniority level is required")
    private SeniorityLevel seniorityLevel;

    @NotNull(message = "Capacity percentage is required")
    @Min(value = 0, message = "Capacity percentage must be at least 0")
    @Max(value = 100, message = "Capacity percentage must not exceed 100")
    private Integer capacityPercentage;

    private String bio;

    public String getJobTitle() { return jobTitle; }
    public String getDepartment() { return department; }
    public SeniorityLevel getSeniorityLevel() { return seniorityLevel; }
    public Integer getCapacityPercentage() { return capacityPercentage; }
    public String getBio() { return bio; }
}
