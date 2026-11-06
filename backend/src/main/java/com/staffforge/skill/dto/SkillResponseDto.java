package com.staffforge.skill.dto;

import com.staffforge.skill.Skill;
import java.time.LocalDateTime;

public class SkillResponseDto {

    private Long id;
    private String name;
    private String category;
    private String description;
    private LocalDateTime createdAt;

    public SkillResponseDto() {}

    public SkillResponseDto(Long id, String name, String category, String description, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static SkillResponseDto fromEntity(Skill skill) {
        if (skill == null) {
            return null;
        }
        return new SkillResponseDto(
                skill.getId(),
                skill.getName(),
                skill.getCategory(),
                skill.getDescription(),
                skill.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
