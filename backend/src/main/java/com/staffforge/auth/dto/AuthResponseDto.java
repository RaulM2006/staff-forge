package com.staffforge.auth.dto;

public class AuthResponseDto {

    private String token;
    private String type = "Bearer";
    private UserResponseDto user;

    public AuthResponseDto() {}

    public AuthResponseDto(String token, UserResponseDto user) {
        this.token = token;
        this.type = "Bearer";
        this.user = user;
    }

    public AuthResponseDto(String token, String type, UserResponseDto user) {
        this.token = token;
        this.type = type != null ? type : "Bearer";
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserResponseDto getUser() {
        return user;
    }

    public void setUser(UserResponseDto user) {
        this.user = user;
    }
}
