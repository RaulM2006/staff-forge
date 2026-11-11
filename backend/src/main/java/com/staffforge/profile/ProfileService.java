package com.staffforge.profile;

import com.staffforge.auth.exception.UserNotFoundException;
import com.staffforge.employee.EmployeeProfile;
import com.staffforge.employee.EmployeeProfileRepository;
import com.staffforge.employee.EmployeeSkill;
import com.staffforge.employee.EmployeeSkillRepository;
import com.staffforge.profile.dto.AddSkillRequestDto;
import com.staffforge.profile.dto.EmployeeSkillResponseDto;
import com.staffforge.profile.dto.ProfileResponseDto;
import com.staffforge.profile.dto.ProfileUpdateRequestDto;
import com.staffforge.profile.exception.ProfileNotFoundException;
import com.staffforge.profile.exception.SkillAlreadyAssociatedException;
import com.staffforge.profile.exception.SkillNotAssociatedException;
import com.staffforge.skill.Skill;
import com.staffforge.skill.SkillRepository;
import com.staffforge.skill.exception.SkillNotFoundException;
import com.staffforge.user.User;
import com.staffforge.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final SkillRepository skillRepository;

    public ProfileService(UserRepository userRepository,
                          EmployeeProfileRepository employeeProfileRepository,
                          EmployeeSkillRepository employeeSkillRepository,
                          SkillRepository skillRepository) {
        this.userRepository = userRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.employeeSkillRepository = employeeSkillRepository;
        this.skillRepository = skillRepository;
    }

    /**
     * Returns the full profile (including skills) of the currently authenticated user.
     *
     * @param email the email extracted from the JWT (SecurityContext subject)
     */
    public ProfileResponseDto getMyProfile(String email) {
        EmployeeProfile profile = findProfileByEmail(email);
        return ProfileResponseDto.fromEntity(profile);
    }

    /**
     * Updates the basic details of the currently authenticated user's profile.
     *
     * @param email the email extracted from the JWT
     * @param dto   the update payload
     */
    @Transactional
    public ProfileResponseDto updateMyProfile(String email, ProfileUpdateRequestDto dto) {
        EmployeeProfile profile = findProfileByEmail(email);

        profile.setJobTitle(dto.getJobTitle());
        profile.setDepartment(dto.getDepartment());
        profile.setSeniorityLevel(dto.getSeniorityLevel());
        profile.setCapacityPercentage(dto.getCapacityPercentage());
        profile.setBio(dto.getBio());

        EmployeeProfile saved = employeeProfileRepository.save(profile);
        return ProfileResponseDto.fromEntity(saved);
    }

    /**
     * Adds a skill (from the global catalog) to the authenticated user's profile.
     *
     * @param email the email extracted from the JWT
     * @param dto   contains skillId, proficiencyLevel, yearsOfExperience
     */
    @Transactional
    public EmployeeSkillResponseDto addSkillToMyProfile(String email, AddSkillRequestDto dto) {
        EmployeeProfile profile = findProfileByEmail(email);

        if (employeeSkillRepository.existsByEmployeeProfileIdAndSkillId(profile.getId(), dto.getSkillId())) {
            throw new SkillAlreadyAssociatedException(
                    "Skill with id " + dto.getSkillId() + " is already associated with your profile.");
        }

        Skill skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with id: " + dto.getSkillId()));

        EmployeeSkill employeeSkill = new EmployeeSkill(
                profile,
                skill,
                dto.getProficiencyLevel(),
                dto.getYearsOfExperience()
        );

        EmployeeSkill savedSkill = employeeSkillRepository.save(employeeSkill);
        profile.getEmployeeSkills().add(savedSkill);

        return EmployeeSkillResponseDto.fromEntity(savedSkill);
    }

    /**
     * Removes a skill from the authenticated user's profile.
     *
     * @param email   the email extracted from the JWT
     * @param skillId the ID of the global Skill to remove
     */
    @Transactional
    public void removeSkillFromMyProfile(String email, Long skillId) {
        EmployeeProfile profile = findProfileByEmail(email);

        EmployeeSkill employeeSkill = employeeSkillRepository
                .findByEmployeeProfileIdAndSkillId(profile.getId(), skillId)
                .orElseThrow(() -> new SkillNotAssociatedException(
                        "Skill with id " + skillId + " is not associated with your profile."));

        profile.getEmployeeSkills().remove(employeeSkill);
        employeeSkillRepository.delete(employeeSkill);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private EmployeeProfile findProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return employeeProfileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException(
                        "No employee profile found for user: " + email
                        + ". Please contact your administrator to create your profile."));
    }
}
