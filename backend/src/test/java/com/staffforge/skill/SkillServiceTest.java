package com.staffforge.skill;

import com.staffforge.skill.dto.SkillRequestDto;
import com.staffforge.skill.dto.SkillResponseDto;
import com.staffforge.skill.exception.SkillAlreadyExistsException;
import com.staffforge.skill.exception.SkillNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    private Skill javaSkill;

    @BeforeEach
    void setUp() {
        javaSkill = new Skill("Java", "Backend", "Core Java programming language");
        javaSkill.setId(1L);
    }

    @Nested
    @DisplayName("getAllSkills Tests")
    class GetAllSkillsTests {

        @Test
        @DisplayName("Should return all skills converted to DTOs")
        void shouldReturnAllSkills() {
            Skill springSkill = new Skill("Spring Boot", "Backend", "Framework");
            springSkill.setId(2L);

            when(skillRepository.findAll()).thenReturn(List.of(javaSkill, springSkill));

            List<SkillResponseDto> result = skillService.getAllSkills();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Java", result.get(0).getName());
            assertEquals("Spring Boot", result.get(1).getName());
            verify(skillRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no skills exist")
        void shouldReturnEmptyListWhenNoSkills() {
            when(skillRepository.findAll()).thenReturn(Collections.emptyList());

            List<SkillResponseDto> result = skillService.getAllSkills();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(skillRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getSkillById Tests")
    class GetSkillByIdTests {

        @Test
        @DisplayName("Should return SkillResponseDto when skill is found")
        void shouldReturnSkillWhenFound() {
            when(skillRepository.findById(1L)).thenReturn(Optional.of(javaSkill));

            SkillResponseDto result = skillService.getSkillById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Java", result.getName());
            assertEquals("Backend", result.getCategory());
        }

        @Test
        @DisplayName("Should throw SkillNotFoundException when skill does not exist")
        void shouldThrowExceptionWhenNotFound() {
            when(skillRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(SkillNotFoundException.class, () -> skillService.getSkillById(999L));
        }
    }

    @Nested
    @DisplayName("createSkill Tests")
    class CreateSkillTests {

        @Test
        @DisplayName("Should successfully create and return new skill")
        void shouldCreateSkillSuccessfully() {
            SkillRequestDto request = new SkillRequestDto("Kotlin", "Backend", "Modern JVM language");
            Skill savedSkill = new Skill("Kotlin", "Backend", "Modern JVM language");
            savedSkill.setId(3L);

            when(skillRepository.existsByNameIgnoreCase("Kotlin")).thenReturn(false);
            when(skillRepository.save(any(Skill.class))).thenReturn(savedSkill);

            SkillResponseDto response = skillService.createSkill(request);

            assertNotNull(response);
            assertEquals(3L, response.getId());
            assertEquals("Kotlin", response.getName());
            assertEquals("Backend", response.getCategory());
            verify(skillRepository).save(any(Skill.class));
        }

        @Test
        @DisplayName("Should trim whitespace when creating a skill")
        void shouldTrimWhitespaceOnCreate() {
            SkillRequestDto request = new SkillRequestDto("  Kotlin  ", "  Backend  ", "Modern JVM language");
            Skill savedSkill = new Skill("Kotlin", "Backend", "Modern JVM language");
            savedSkill.setId(3L);

            when(skillRepository.existsByNameIgnoreCase("Kotlin")).thenReturn(false);
            when(skillRepository.save(any(Skill.class))).thenReturn(savedSkill);

            SkillResponseDto response = skillService.createSkill(request);

            assertNotNull(response);
            assertEquals("Kotlin", response.getName());
            verify(skillRepository).existsByNameIgnoreCase("Kotlin");
        }

        @Test
        @DisplayName("Should throw SkillAlreadyExistsException when skill name already exists")
        void shouldThrowExceptionWhenSkillNameAlreadyExists() {
            SkillRequestDto request = new SkillRequestDto("Java", "Backend", "Duplicate description");

            when(skillRepository.existsByNameIgnoreCase("Java")).thenReturn(true);

            assertThrows(SkillAlreadyExistsException.class, () -> skillService.createSkill(request));
            verify(skillRepository, never()).save(any(Skill.class));
        }
    }

    @Nested
    @DisplayName("updateSkill Tests")
    class UpdateSkillTests {

        @Test
        @DisplayName("Should successfully update existing skill")
        void shouldUpdateSkillSuccessfully() {
            SkillRequestDto request = new SkillRequestDto("Java 21", "Programming", "Updated description");

            when(skillRepository.findById(1L)).thenReturn(Optional.of(javaSkill));
            when(skillRepository.findByNameIgnoreCase("Java 21")).thenReturn(Optional.empty());
            when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));

            SkillResponseDto response = skillService.updateSkill(1L, request);

            assertNotNull(response);
            assertEquals("Java 21", response.getName());
            assertEquals("Programming", response.getCategory());
            assertEquals("Updated description", response.getDescription());
        }

        @Test
        @DisplayName("Should allow updating without changing name")
        void shouldAllowUpdatingSameName() {
            SkillRequestDto request = new SkillRequestDto("Java", "Programming", "New description");

            when(skillRepository.findById(1L)).thenReturn(Optional.of(javaSkill));
            when(skillRepository.findByNameIgnoreCase("Java")).thenReturn(Optional.of(javaSkill));
            when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));

            SkillResponseDto response = skillService.updateSkill(1L, request);

            assertNotNull(response);
            assertEquals("Java", response.getName());
            assertEquals("Programming", response.getCategory());
        }

        @Test
        @DisplayName("Should throw SkillNotFoundException when updating non-existent skill")
        void shouldThrowWhenSkillToUpdateNotFound() {
            SkillRequestDto request = new SkillRequestDto("Go", "Backend", "Go language");

            when(skillRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(SkillNotFoundException.class, () -> skillService.updateSkill(99L, request));
            verify(skillRepository, never()).save(any(Skill.class));
        }

        @Test
        @DisplayName("Should throw SkillAlreadyExistsException when renaming to existing skill name of another skill")
        void shouldThrowWhenRenamingToAnotherExistingSkill() {
            Skill anotherSkill = new Skill("Python", "Data", "Python language");
            anotherSkill.setId(2L);

            SkillRequestDto request = new SkillRequestDto("Python", "Backend", "Trying to take Python name");

            when(skillRepository.findById(1L)).thenReturn(Optional.of(javaSkill));
            when(skillRepository.findByNameIgnoreCase("Python")).thenReturn(Optional.of(anotherSkill));

            assertThrows(SkillAlreadyExistsException.class, () -> skillService.updateSkill(1L, request));
            verify(skillRepository, never()).save(any(Skill.class));
        }
    }

    @Nested
    @DisplayName("deleteSkill Tests")
    class DeleteSkillTests {

        @Test
        @DisplayName("Should delete skill successfully when found")
        void shouldDeleteSkillSuccessfully() {
            when(skillRepository.findById(1L)).thenReturn(Optional.of(javaSkill));

            assertDoesNotThrow(() -> skillService.deleteSkill(1L));

            verify(skillRepository, times(1)).delete(javaSkill);
        }

        @Test
        @DisplayName("Should throw SkillNotFoundException when deleting non-existent skill")
        void shouldThrowWhenDeletingNonExistentSkill() {
            when(skillRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(SkillNotFoundException.class, () -> skillService.deleteSkill(99L));
            verify(skillRepository, never()).delete(any(Skill.class));
        }
    }
}
