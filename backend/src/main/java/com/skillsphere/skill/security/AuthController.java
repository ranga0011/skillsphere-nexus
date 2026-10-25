package com.skillsphere.skill.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.skillsphere.skill.security.AuthModels.AccountCreatedResponse;
import com.skillsphere.skill.security.AuthModels.AuthResponse;
import com.skillsphere.skill.security.AuthModels.CreateEmployeeAccountRequest;
import com.skillsphere.skill.security.AuthModels.CreateStudentAccountRequest;
import com.skillsphere.skill.security.AuthModels.CurrentUserResponse;
import com.skillsphere.skill.security.AuthModels.LoginRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
        return new CurrentUserResponse(jwt.getSubject(), jwt.getClaimAsString("role"),
                JwtService.employeeIdFromClaims(jwt.getClaims()),
                JwtService.studentIdFromClaims(jwt.getClaims()));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountCreatedResponse createEmployeeAccount(@Valid @RequestBody CreateEmployeeAccountRequest request) {
        return authService.createEmployeeAccount(request);
    }

    @PostMapping("/students")
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountCreatedResponse createStudentAccount(@Valid @RequestBody CreateStudentAccountRequest request) {
        return authService.createStudentAccount(request);
    }
}