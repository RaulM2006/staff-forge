package com.staffforge.profile;

import com.staffforge.profile.dto.AddSkillRequestDto;
import com.staffforge.profile.dto.EmployeeSkillResponseDto;
import com.staffforge.profile.dto.ProfileResponseDto;
import com.staffforge.profile.dto.ProfileUpdateRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * GET /api/profiles/me
     * Returns the full profile (details + skills) of the currently authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        ProfileResponseDto profile = profileService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/profiles/me
     * Updates the basic details of the currently authenticated user's profile.
     */
    @PutMapping("/me")
    public ResponseEntity<ProfileResponseDto> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequestDto dto) {
        ProfileResponseDto updated = profileService.updateMyProfile(userDetails.getUsername(), dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * POST /api/profiles/me/skills
     * Adds a skill from the global catalog to the authenticated user's profile.
     */
    @PostMapping("/me/skills")
    public ResponseEntity<EmployeeSkillResponseDto> addSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddSkillRequestDto dto) {
        EmployeeSkillResponseDto added = profileService.addSkillToMyProfile(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    /**
     * DELETE /api/profiles/me/skills/{skillId}
     * Removes a skill from the authenticated user's profile.
     *
     * @param skillId the global Skill ID (not the EmployeeSkill join-table ID)
     */
    @DeleteMapping("/me/skills/{skillId}")
    public ResponseEntity<Void> removeSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long skillId) {
        profileService.removeSkillFromMyProfile(userDetails.getUsername(), skillId);
        return ResponseEntity.noContent().build();
    }
}
