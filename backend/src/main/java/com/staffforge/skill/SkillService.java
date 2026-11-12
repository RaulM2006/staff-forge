package com.staffforge.skill;

import com.staffforge.skill.dto.SkillRequestDto;
import com.staffforge.skill.dto.SkillResponseDto;
import com.staffforge.skill.exception.SkillAlreadyExistsException;
import com.staffforge.skill.exception.SkillNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<SkillResponseDto> getAllSkills() {
        return skillRepository.findAll()
                .stream()
                .map(SkillResponseDto::fromEntity)
                .toList();
    }

    public SkillResponseDto getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new SkillNotFoundException(id));
        return SkillResponseDto.fromEntity(skill);
    }

    @Transactional
    public SkillResponseDto createSkill(SkillRequestDto request) {
        String trimmedName = request.getName().trim();

        if (skillRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new SkillAlreadyExistsException("Skill with name '" + trimmedName + "' already exists");
        }

        String category = request.getCategory() != null ? request.getCategory().trim() : null;
        Skill skill = new Skill(trimmedName, category, request.getDescription());
        Skill saved = skillRepository.save(skill);

        return SkillResponseDto.fromEntity(saved);
    }

    @Transactional
    public SkillResponseDto updateSkill(Long id, SkillRequestDto request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new SkillNotFoundException(id));

        String trimmedName = request.getName().trim();
        skillRepository.findByNameIgnoreCase(trimmedName).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new SkillAlreadyExistsException("Skill with name '" + trimmedName + "' already exists");
            }
        });

        skill.setName(trimmedName);
        skill.setCategory(request.getCategory() != null ? request.getCategory().trim() : null);
        skill.setDescription(request.getDescription());

        Skill updated = skillRepository.save(skill);
        return SkillResponseDto.fromEntity(updated);
    }

    @Transactional
    public void deleteSkill(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new SkillNotFoundException(id));
        skillRepository.delete(skill);
    }
}
