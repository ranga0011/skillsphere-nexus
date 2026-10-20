package com.skillsphere.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.skillsphere.skill.security.AppUser;
import com.skillsphere.skill.security.AppUserRepository;
import com.skillsphere.skill.security.UserRole;

@SpringBootTest(properties = {
        "spring.profiles.active=local",
        "INITIAL_ADMIN_USERNAME=bootstrap-admin@example.test",
        "INITIAL_ADMIN_PASSWORD=Test-Only-Admin-Password-42"
})
@AutoConfigureMockMvc
class InitialAdminEnvironmentIntegrationTests extends IsolatedIntegrationTest {
    private static final String USERNAME = "bootstrap-admin@example.test";
    private static final String PASSWORD = "Test-Only-Admin-Password-42";

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void launcherEnvironmentNamesCreateBcryptAdminAndPermitLogin() throws Exception {
        AppUser user = appUserRepository.findByUsername(USERNAME).orElseThrow();
        assertEquals(UserRole.HR_ADMIN, user.getRole());
        assertNotEquals(PASSWORD, user.getPasswordHash());
        assertTrue(passwordEncoder.matches(PASSWORD, user.getPasswordHash()));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"bootstrap-admin@example.test","password":"Test-Only-Admin-Password-42"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("HR_ADMIN"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"bootstrap-admin@example.test","password":"incorrect-test-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}