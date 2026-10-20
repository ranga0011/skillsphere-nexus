package com.skillsphere.skill;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

abstract class IsolatedIntegrationTest {
    private static final String DATABASE_NAME = UUID.randomUUID().toString().replace("-", "");
    private static final String JWT_SECRET = createTestSecret();

    @DynamicPropertySource
    @SuppressWarnings("unused")
    static void configureIsolatedDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:" + DATABASE_NAME + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("app.security.jwt.secret", () -> JWT_SECRET);
    }

    private static String createTestSecret() {
        byte[] secret = new byte[48];
        new SecureRandom().nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }
}