package com.skillsphere.skill.security;

import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.security.AuthModels.AccountCreatedResponse;
import com.skillsphere.skill.security.AuthModels.AuthResponse;
import com.skillsphere.skill.security.AuthModels.CreateEmployeeAccountRequest;
import com.skillsphere.skill.security.AuthModels.LoginRequest;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AppUserRepository appUserRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository, EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        if (request == null || request.username() == null || request.password() == null) {
            throw invalidCredentials();
        }
        AppUser user = appUserRepository.findByUsername(normalizeUsername(request.username())).orElse(null);
        if (user == null) {
            logger.warn("Login rejected because no account matched the normalized username");
            throw invalidCredentials();
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            logger.warn("Login rejected because password verification failed");
            throw invalidCredentials();
        }
        return new AuthResponse(jwtService.createToken(user), "Bearer", JwtService.TOKEN_LIFETIME_SECONDS,
                user.getUsername(), user.getRole().name(), user.getEmployeeId());
    }

    @Transactional
    public AccountCreatedResponse createEmployeeAccount(CreateEmployeeAccountRequest request) {
        if (request == null || request.username() == null || request.password() == null
            || request.password().length() < 12
            || request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Username, employeeId, and a password of at least 12 characters are required");
        }
        String username = normalizeUsername(request.username());
        if (appUserRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already registered");
        }
        if (!employeeRepository.existsById(request.employeeId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee record was not found");
        }
        if (appUserRepository.existsByEmployeeId(request.employeeId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee already has an account");
        }
        appUserRepository.save(new AppUser(username, passwordEncoder.encode(request.password()),
                UserRole.EMPLOYEE, request.employeeId()));
        return new AccountCreatedResponse(username, UserRole.EMPLOYEE.name(), request.employeeId());
    }

    public static String normalizeUsername(String username) {
        return username.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}