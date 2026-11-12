package com.staffforge.user;

import com.staffforge.allocation.Allocation;
import com.staffforge.allocation.AllocationRepository;
import com.staffforge.employee.*;
import com.staffforge.project.*;
import com.staffforge.skill.Skill;
import com.staffforge.skill.SkillRepository;
import com.staffforge.BackendApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class)
@Transactional
class UserEntityAndRelationshipTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeProfileRepository employeeProfileRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectPositionRepository projectPositionRepository;

    @Autowired
    private AllocationRepository allocationRepository;

    @Test
    @DisplayName("Should create and retrieve User by email and role")
    void testCreateAndFindUser() {
        User user = new User("alice@staffforge.com", "hashed_pwd", "Alice", "Smith", Role.PROJECT_MANAGER);
        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("alice@staffforge.com", savedUser.getEmail());
        assertEquals(Role.PROJECT_MANAGER, savedUser.getRole());
        assertTrue(savedUser.isEnabled());
        assertNotNull(savedUser.getCreatedAt());

        Optional<User> found = userRepository.findByEmail("alice@staffforge.com");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getFirstName());

        List<User> pms = userRepository.findByRole(Role.PROJECT_MANAGER);
        assertFalse(pms.isEmpty());
    }

    @Test
    @DisplayName("Should create EmployeeProfile with 1:1 relationship to User")
    void testCreateEmployeeProfile() {
        User user = new User("bob@staffforge.com", "hashed_pwd", "Bob", "Jones", Role.EMPLOYEE);
        user = userRepository.save(user);

        EmployeeProfile profile = new EmployeeProfile(user, "Senior Java Developer", "Engineering", SeniorityLevel.SENIOR);
        profile.setCapacityPercentage(80);
        profile.setBio("Passionate backend engineer with 8+ years of experience.");
        EmployeeProfile savedProfile = employeeProfileRepository.save(profile);

        assertNotNull(savedProfile.getId());
        assertEquals(user.getId(), savedProfile.getUser().getId());
        assertEquals("Senior Java Developer", savedProfile.getJobTitle());
        assertEquals(80, savedProfile.getCapacityPercentage());

        Optional<EmployeeProfile> foundByUserId = employeeProfileRepository.findByUserId(user.getId());
        assertTrue(foundByUserId.isPresent());
        assertEquals(SeniorityLevel.SENIOR, foundByUserId.get().getSeniorityLevel());
    }

    @Test
    @DisplayName("Should create Skills and associate them via EmployeeSkill")
    void testSkillAndEmployeeSkillMapping() {
        User user = new User("carol@staffforge.com", "hashed_pwd", "Carol", "White", Role.EMPLOYEE);
        user = userRepository.save(user);

        EmployeeProfile profile = new EmployeeProfile(user, "Full Stack Engineer", "Engineering", SeniorityLevel.MID);
        profile = employeeProfileRepository.save(profile);

        Skill javaSkill = skillRepository.save(new Skill("Java", "Backend", "Object-oriented language"));
        Skill angularSkill = skillRepository.save(new Skill("Angular", "Frontend", "TypeScript framework"));

        EmployeeSkill empSkill1 = new EmployeeSkill(profile, javaSkill, ProficiencyLevel.ADVANCED, 4);
        EmployeeSkill empSkill2 = new EmployeeSkill(profile, angularSkill, ProficiencyLevel.INTERMEDIATE, 2);

        employeeSkillRepository.save(empSkill1);
        employeeSkillRepository.save(empSkill2);

        List<EmployeeSkill> profileSkills = employeeSkillRepository.findByEmployeeProfileId(profile.getId());
        assertEquals(2, profileSkills.size());

        List<EmployeeSkill> javaEmployees = employeeSkillRepository.findBySkillId(javaSkill.getId());
        assertEquals(1, javaEmployees.size());
        assertEquals(ProficiencyLevel.ADVANCED, javaEmployees.get(0).getProficiencyLevel());

        assertTrue(employeeSkillRepository.existsByEmployeeProfileIdAndSkillId(profile.getId(), javaSkill.getId()));
    }

    @Test
    @DisplayName("Should connect User with Allocation and ProjectPosition")
    void testUserAllocationRelationship() {
        // Create User
        User engineer = userRepository.save(new User("dave@staffforge.com", "hashed_pwd", "Dave", "Miller", Role.EMPLOYEE));

        // Create Project & Position
        Project project = new Project("StaffForge Alpha", "Core platform MVP", LocalDate.now(), LocalDate.now().plusMonths(6), ProjectStatus.ACTIVE);
        project = projectRepository.save(project);

        ProjectPosition position = new ProjectPosition(project, "Backend Lead", "Oversee API development", SeniorityLevel.LEAD);
        position = projectPositionRepository.save(position);

        // Allocate User to Position
        Allocation allocation = new Allocation(position, engineer, 50, LocalDate.now(), LocalDate.now().plusMonths(3));
        allocation = allocationRepository.save(allocation);

        assertNotNull(allocation.getId());
        assertEquals(engineer.getId(), allocation.getUserId());
        assertEquals(engineer.getId(), allocation.getUser().getId());
        assertEquals(position.getId(), allocation.getPosition().getId());

        // Verify repository lookups
        List<Allocation> byUserId = allocationRepository.findByUserId(engineer.getId());
        assertEquals(1, byUserId.size());
        assertEquals(50, byUserId.get(0).getAllocationPercentage());

        List<Allocation> byUser = allocationRepository.findByUser(engineer);
        assertEquals(1, byUser.size());
        assertEquals(allocation.getId(), byUser.get(0).getId());
    }
}
