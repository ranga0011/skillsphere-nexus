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

        public record CreateStudentAccountRequest(
                @NotBlank @Email @Size(max = 150) String username,
                @NotBlank @Size(min = 12, max = 72) String password,
                @NotBlank @Size(max = 100) String name,
                @NotBlank @Email @Size(max = 150) String email,
                @NotBlank @Size(max = 150) String program) {
        }

        public record AuthResponse(String accessToken, String tokenType, long expiresIn,
                String username, String role, Long employeeId, Long studentId) {
        }

        public record CurrentUserResponse(String username, String role, Long employeeId, Long studentId) {
        }

        public record AccountCreatedResponse(String username, String role, Long employeeId, Long studentId) {
        }
}