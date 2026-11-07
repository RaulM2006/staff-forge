package com.staffforge.auth.dto;

import com.staffforge.user.Role;
import com.staffforge.user.User;

import java.util.Collections;
import java.util.List;

public class UserResponseDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private List<String> roles;
    private boolean enabled;

    public UserResponseDto() {}

    public UserResponseDto(Long id, String email, String firstName, String lastName, Role role, List<String> roles, boolean enabled) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.roles = roles;
        this.enabled = enabled;
    }

    public static UserResponseDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        String roleName = user.getRole() != null ? user.getRole().name() : Role.EMPLOYEE.name();
        List<String> rolesList = List.of("ROLE_" + roleName, roleName);

        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                rolesList,
                user.isEnabled()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<String> getRoles() {
        return roles != null ? roles : Collections.emptyList();
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
