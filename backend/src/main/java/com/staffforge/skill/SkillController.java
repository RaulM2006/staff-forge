package com.staffforge.skill;

import com.staffforge.skill.dto.SkillRequestDto;
import com.staffforge.skill.dto.SkillResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public ResponseEntity<List<SkillResponseDto>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<SkillResponseDto> createSkill(@Valid @RequestBody SkillRequestDto request) {
        SkillResponseDto created = skillService.createSkill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<SkillResponseDto> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequestDto request) {
        SkillResponseDto updated = skillService.updateSkill(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }
}
