package com.staffforge.employee;

import com.staffforge.project.SeniorityLevel;
import com.staffforge.skill.Skill;
import com.staffforge.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "employee_profiles")
public class EmployeeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    @Column(length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "seniority_level", nullable = false, length = 30)
    private SeniorityLevel seniorityLevel;

    @Column(name = "capacity_percentage", nullable = false)
    private Integer capacityPercentage = 100;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeSkill> employeeSkills = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.capacityPercentage == null) {
            this.capacityPercentage = 100;
        }
    }

    public EmployeeProfile() {}

    public EmployeeProfile(User user, String jobTitle, String department, SeniorityLevel seniorityLevel) {
        this.user = user;
        this.jobTitle = jobTitle;
        this.department = department;
        this.seniorityLevel = seniorityLevel;
        this.capacityPercentage = 100;
    }

    public EmployeeProfile(User user, String jobTitle, String department, SeniorityLevel seniorityLevel, Integer capacityPercentage, String bio) {
        this.user = user;
        this.jobTitle = jobTitle;
        this.department = department;
        this.seniorityLevel = seniorityLevel;
        this.capacityPercentage = capacityPercentage != null ? capacityPercentage : 100;
        this.bio = bio;
    }

    public void addSkill(Skill skill, ProficiencyLevel proficiencyLevel, Integer yearsOfExperience) {
        EmployeeSkill employeeSkill = new EmployeeSkill(this, skill, proficiencyLevel, yearsOfExperience);
        employeeSkills.add(employeeSkill);
        skill.getEmployeeSkills().add(employeeSkill);
    }

    public void removeSkill(EmployeeSkill employeeSkill) {
        employeeSkills.remove(employeeSkill);
        if (employeeSkill.getSkill() != null) {
            employeeSkill.getSkill().getEmployeeSkills().remove(employeeSkill);
        }
        employeeSkill.setEmployeeProfile(null);
        employeeSkill.setSkill(null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public SeniorityLevel getSeniorityLevel() { return seniorityLevel; }
    public void setSeniorityLevel(SeniorityLevel seniorityLevel) { this.seniorityLevel = seniorityLevel; }
    public Integer getCapacityPercentage() { return capacityPercentage; }
    public void setCapacityPercentage(Integer capacityPercentage) { this.capacityPercentage = capacityPercentage; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public List<EmployeeSkill> getEmployeeSkills() { return employeeSkills; }
    public void setEmployeeSkills(List<EmployeeSkill> employeeSkills) { this.employeeSkills = employeeSkills; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmployeeProfile that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
