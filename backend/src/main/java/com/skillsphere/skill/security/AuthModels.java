package com.skillsphere.skill.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthModels {
    private AuthModels() {
    }

        public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

        public record CreateEmployeeAccountRequest(
            @NotBlank @Email @Size(max = 150) String username,
            @NotBlank @Size(min = 12, max = 72) String password,
            @NotNull Long employeeId) {
    }

    public record AuthResponse(String accessToken, String tokenType, long expiresIn,
            String username, String role, Long employeeId) {
    }

    public record CurrentUserResponse(String username, String role, Long employeeId) {
    }

    public record AccountCreatedResponse(String username, String role, Long employeeId) {
    }
}