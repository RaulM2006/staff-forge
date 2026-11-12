package com.staffforge.profile.dto;

import com.staffforge.employee.EmployeeProfile;

import java.time.LocalDateTime;
import java.util.List;

public class ProfileResponseDto {

    private Long id;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String jobTitle;
    private String department;
    private String seniorityLevel;
    private Integer capacityPercentage;
    private String bio;
    private LocalDateTime createdAt;
    private List<EmployeeSkillResponseDto> skills;

    private ProfileResponseDto() {}

    public static ProfileResponseDto fromEntity(EmployeeProfile profile) {
        ProfileResponseDto dto = new ProfileResponseDto();
        dto.id = profile.getId();
        dto.userId = profile.getUser().getId();
        dto.email = profile.getUser().getEmail();
        dto.firstName = profile.getUser().getFirstName();
        dto.lastName = profile.getUser().getLastName();
        dto.role = profile.getUser().getRole().name();
        dto.jobTitle = profile.getJobTitle();
        dto.department = profile.getDepartment();
        dto.seniorityLevel = profile.getSeniorityLevel().name();
        dto.capacityPercentage = profile.getCapacityPercentage();
        dto.bio = profile.getBio();
        dto.createdAt = profile.getCreatedAt();
        dto.skills = profile.getEmployeeSkills().stream()
                .map(EmployeeSkillResponseDto::fromEntity)
                .toList();
        return dto;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
    public String getJobTitle() { return jobTitle; }
    public String getDepartment() { return department; }
    public String getSeniorityLevel() { return seniorityLevel; }
    public Integer getCapacityPercentage() { return capacityPercentage; }
    public String getBio() { return bio; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<EmployeeSkillResponseDto> getSkills() { return skills; }
}
