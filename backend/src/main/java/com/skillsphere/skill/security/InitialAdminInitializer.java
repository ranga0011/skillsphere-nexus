package com.skillsphere.skill.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InitialAdminInitializer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(InitialAdminInitializer.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public InitialAdminInitializer(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder,
            @Value("${app.security.initial-admin.username:${INITIAL_ADMIN_USERNAME:}}") String username,
            @Value("${app.security.initial-admin.password:${INITIAL_ADMIN_PASSWORD:}}") String password) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() && password.isBlank()) {
            return;
        }
        if (username.isBlank() || password.length() < 12
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Set both initial admin variables and use a password of 12 to 72 UTF-8 bytes");
        }
        String normalizedUsername = AuthService.normalizeUsername(username);
        AppUser existingUser = appUserRepository.findByUsername(normalizedUsername).orElse(null);
        if (existingUser != null && existingUser.getRole() == UserRole.HR_ADMIN) {
            existingUser.updatePasswordHash(passwordEncoder.encode(password));
            appUserRepository.save(existingUser);
            logger.info("Initial HR admin credentials applied to the configured account");
            return;
        }
        if (existingUser != null) {
            throw new IllegalStateException("Initial admin username belongs to a non-admin account; choose a different email");
        }
        if (appUserRepository.existsByRole(UserRole.HR_ADMIN)) {
            throw new IllegalStateException("An HR admin already exists; initial admin bootstrap was refused");
        }
        appUserRepository.save(new AppUser(normalizedUsername, passwordEncoder.encode(password),
                UserRole.HR_ADMIN, null));
        logger.info("Initial HR admin account created");
    }
}