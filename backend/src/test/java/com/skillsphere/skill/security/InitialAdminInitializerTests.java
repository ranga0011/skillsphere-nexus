package com.skillsphere.skill.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class InitialAdminInitializerTests {
    @Test
    void createsFirstAdminWithBcryptHashOnlyInLocalProfile() {
        AppUserRepository repository = org.mockito.Mockito.mock(AppUserRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String username = "chosen-admin@example.com";
        String password = "test-only-admin-password";
        when(repository.findByUsername(username)).thenReturn(Optional.empty());
        when(repository.existsByRole(UserRole.HR_ADMIN)).thenReturn(false);

        new InitialAdminInitializer(repository, passwordEncoder, username, password).run(null);

        var captor = org.mockito.ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(captor.capture());
        AppUser savedUser = captor.getValue();
        assertEquals(username, savedUser.getUsername());
        assertEquals(UserRole.HR_ADMIN, savedUser.getRole());
        assertEquals(null, savedUser.getEmployeeId());
        assertFalse(password.equals(savedUser.getPasswordHash()));
        assertTrue(passwordEncoder.matches(password, savedUser.getPasswordHash()));
        assertEquals("local", InitialAdminInitializer.class.getAnnotation(Profile.class).value()[0]);
    }

    @Test
    void appliesConfiguredPasswordToMatchingExistingAdminWithoutReplacingAccount() {
        AppUserRepository repository = org.mockito.Mockito.mock(AppUserRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String configuredPassword = "new-test-admin-password";
        AppUser existingAdmin = new AppUser("admin@example.com",
                passwordEncoder.encode("existing-test-password"), UserRole.HR_ADMIN, null);
        when(repository.findByUsername("admin@example.com")).thenReturn(Optional.of(existingAdmin));

        new InitialAdminInitializer(repository, passwordEncoder, "admin@example.com", configuredPassword).run(null);

        verify(repository).save(existingAdmin);
        verify(repository, never()).existsByRole(UserRole.HR_ADMIN);
        assertTrue(passwordEncoder.matches(configuredPassword, existingAdmin.getPasswordHash()));
        assertFalse(passwordEncoder.matches("existing-test-password", existingAdmin.getPasswordHash()));
    }

    @Test
    void refusesBootstrapWhenAnHrAdminAlreadyExists() {
        AppUserRepository repository = org.mockito.Mockito.mock(AppUserRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        when(repository.findByUsername("second-admin@example.com")).thenReturn(Optional.empty());
        when(repository.existsByRole(UserRole.HR_ADMIN)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> new InitialAdminInitializer(repository, passwordEncoder,
                        "second-admin@example.com", "test-only-admin-password").run(null));
        verify(repository, never()).save(any(AppUser.class));
    }

    @Test
    void refusesToReplaceAnEmployeeWithTheChosenAdminUsername() {
        AppUserRepository repository = org.mockito.Mockito.mock(AppUserRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        when(repository.findByUsername("employee@example.com")).thenReturn(Optional.of(
                new AppUser("employee@example.com", passwordEncoder.encode("existing-test-password"),
                        UserRole.EMPLOYEE, 25L)));

        assertThrows(IllegalStateException.class,
                () -> new InitialAdminInitializer(repository, passwordEncoder,
                        "employee@example.com", "test-only-admin-password").run(null));
        verify(repository, never()).save(any(AppUser.class));
    }
}
