package com.staffforge.project;

import com.staffforge.allocation.Allocation;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A role/slot that a project needs to fill (e.g. "Backend Developer", SENIOR).
 * Allocations assign actual people to a position.
 */
@Entity
@Table(name = "project_positions")
public class ProjectPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "seniority_level", nullable = false, length = 30)
    private SeniorityLevel seniorityLevel;

    @OneToMany(mappedBy = "position")
    private List<Allocation> allocations = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ProjectPosition() {}

    public ProjectPosition(Project project, String title, String description, SeniorityLevel seniorityLevel) {
        this.project = project;
        this.title = title;
        this.description = description;
        this.seniorityLevel = seniorityLevel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public SeniorityLevel getSeniorityLevel() { return seniorityLevel; }
    public void setSeniorityLevel(SeniorityLevel seniorityLevel) { this.seniorityLevel = seniorityLevel; }
    public List<Allocation> getAllocations() { return allocations; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectPosition that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
