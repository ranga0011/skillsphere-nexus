package com.skillsphere.skill;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.Employee;
import com.skillsphere.skill.entity.Skill;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.repository.SkillRepository;
import com.skillsphere.skill.service.CertificationService;
import com.skillsphere.skill.service.SkillAssessmentService;
import com.skillsphere.skill.security.AppUser;
import com.skillsphere.skill.security.AppUserRepository;
import com.skillsphere.skill.security.UserRole;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTests extends IsolatedIntegrationTest {
    private static final String ADMIN_PASSWORD = "Admin-Test-Password-2026";
    private static final String EMPLOYEE_PASSWORD = "Employee-Test-Password-2026";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillAssessmentService skillAssessmentService;

    @Autowired
    private CertificationService certificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    private Employee employee;
    private Employee anotherEmployee;
    private String adminToken;
    private String employeeToken;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() throws Exception {
        employee = employeeRepository.save(new Employee("Security User", "security.user@example.com", "Engineer", "Engineering"));
        anotherEmployee = employeeRepository.save(new Employee("Other User", "other.user@example.com", "Analyst", "Operations"));
        appUserRepository.save(new AppUser("admin@example.com", passwordEncoder.encode(ADMIN_PASSWORD), UserRole.HR_ADMIN, null));
        appUserRepository.save(new AppUser("employee@example.com", passwordEncoder.encode(EMPLOYEE_PASSWORD), UserRole.EMPLOYEE, employee.getId()));
        adminToken = login("admin@example.com", ADMIN_PASSWORD);
        employeeToken = login("employee@example.com", EMPLOYEE_PASSWORD);
    }

    @Test
    void protectedApiRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void hrAdminCanPerformPermittedOperations() throws Exception {
        mockMvc.perform(post("/api/skills")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Java\",\"category\":\"Technical\",\"description\":\"Language\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Java"));
    }

    @Test
    void employeeCannotPerformHrOnlyOperations() throws Exception {
        mockMvc.perform(post("/api/skills")
                        .header("Authorization", bearer(employeeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Java\",\"category\":\"Technical\",\"description\":\"Language\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeCannotReadAnotherEmployeesRecord() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", anotherEmployee.getId())
                        .header("Authorization", bearer(employeeToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employees/{id}", employee.getId())
                        .header("Authorization", bearer(employeeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("security.user@example.com"));
    }

            @Test
            void employeeCannotReadAnotherEmployeesRestrictedSkillRecords() throws Exception {
            Skill skill = skillRepository.save(new Skill("Security Skill", "Technical", "Test skill"));
            skillAssessmentService.createAssessment(anotherEmployee.getId(), skill.getId(), 80, 100,
                "Practical", "HR Admin", LocalDate.now());
            certificationService.createCertification(anotherEmployee.getId(), "Security Certification", "Example Org",
                "CERT-SEC-1", LocalDate.now(), null, null);

            mockMvc.perform(get("/api/assessments/employee/{id}", anotherEmployee.getId())
                    .header("Authorization", bearer(employeeToken)))
                .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/certifications").param("employeeId", anotherEmployee.getId().toString())
                    .header("Authorization", bearer(employeeToken)))
                .andExpect(status().isForbidden());
            }

            @Test
            void employeeAccountProvisioningIsHrOnlyAndCannotSetItsOwnRole() throws Exception {
            String payload = "{\"username\":\"new.employee@example.com\",\"password\":\"Unique-Employee-Password-2026\",\"employeeId\":%d,\"role\":\"HR_ADMIN\"}"
                .formatted(anotherEmployee.getId());

            mockMvc.perform(post("/api/auth/users")
                    .header("Authorization", bearer(employeeToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/auth/users")
                    .header("Authorization", bearer(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
            }

    @Test
    void studentProvisioningUsesSeparateProfilesAndEnforcesOwnership() throws Exception {
        String firstStudentBody = """
                {"username":"learner.one@example.com","password":"Unique-Student-Password-2026",
                 "name":"Learner One","email":"learner.one@example.com","program":"Data Science",
                 "role":"HR_ADMIN"}
                """;
        String firstResponse = mockMvc.perform(post("/api/auth/students")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstStudentBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.employeeId").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long firstStudentId = responseId(firstResponse, "studentId");
        String studentToken = login("learner.one@example.com", "Unique-Student-Password-2026");

        mockMvc.perform(get("/api/students/me").header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Learner One"))
                .andExpect(jsonPath("$.program").value("Data Science"));

        String secondStudentBody = """
                {"username":"learner.two@example.com","password":"Unique-Student-Password-2026",
                 "name":"Learner Two","email":"learner.two@example.com","program":"Cybersecurity"}
                """;
        String secondResponse = mockMvc.perform(post("/api/auth/students")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondStudentBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long secondStudentId = responseId(secondResponse, "studentId");

        mockMvc.perform(get("/api/students/{id}", secondStudentId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/employees/{id}", employee.getId())
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/students/{id}", firstStudentId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("learner.one@example.com"));

        Skill studentSkill = skillRepository.save(new Skill("Student APIs", "Technical", "HTTP services"));
        mockMvc.perform(post("/api/students/me/skills")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillId":%d,"proficiency":9,"verificationStatus":"VERIFIED"}
                                """.formatted(studentSkill.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verificationStatus").value("PENDING_VERIFICATION"));
        mockMvc.perform(get("/api/certificates").header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        String assessmentResponse = mockMvc.perform(post("/api/assessments")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentId":%d,"skillId":%d,"score":90,"maxScore":100,
                                 "assessmentMethod":"Practical","assessorName":"Admin assessor",
                                 "assessmentDate":"%s","verificationStatus":"VERIFIED"}
                                """.formatted(firstStudentId, studentSkill.getId(), LocalDate.now())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verificationStatus").value("PENDING_VERIFICATION"))
                .andReturn().getResponse().getContentAsString();
        long assessmentId = responseId(assessmentResponse, "id");
        mockMvc.perform(put("/api/assessments/{id}/verification", assessmentId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verificationStatus\":\"VERIFIED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
        mockMvc.perform(get("/api/students/me/skills")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].proficiency").value(9))
                .andExpect(jsonPath("$[0].verificationStatus").value("VERIFIED"));

        String certificates = mockMvc.perform(get("/api/certificates")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].learnerName").value("Learner One"))
                .andReturn().getResponse().getContentAsString();
        long certificateId = responseId(certificates, "id");
        String verificationToken = responseString(certificates, "verificationToken");

        mockMvc.perform(get("/api/certificates/{id}/download", certificateId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
        mockMvc.perform(get("/api/certificates/{id}/download", certificateId)
                        .header("Authorization", bearer(login("learner.two@example.com", "Unique-Student-Password-2026"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/certificates/verify/{token}", verificationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ISSUED"))
                .andExpect(jsonPath("$.learnerName").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist());

        mockMvc.perform(put("/api/certificates/{id}/revoke", certificateId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Assessment evidence was invalidated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));
        mockMvc.perform(get("/api/certificates/verify/{token}", verificationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));
        mockMvc.perform(get("/api/certificates/{id}/download", certificateId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isGone());
    }

    @Test
    void invalidCredentialsAreRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passwordHashesAreNeverIncludedInResponses() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.role").value("HR_ADMIN"));
    }

    @Test
    void corsPreflightAllowsTheFrontendAuthorizationHeader() throws Exception {
        mockMvc.perform(options("/api/employees")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Headers", org.hamcrest.Matchers.containsStringIgnoringCase("authorization")));
    }

    @Test
    void expiredTokensAreRejected() throws Exception {
        Instant now = Instant.now();
        String expired = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().issuer("skillsphere-nexus").subject("admin@example.com")
                        .issuedAt(now.minusSeconds(120)).expiresAt(now.minusSeconds(60))
                        .claim("role", "HR_ADMIN").build())).getTokenValue();

        mockMvc.perform(get("/api/employees").header("Authorization", bearer(expired)))
                .andExpect(status().isUnauthorized());
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        java.util.regex.Matcher tokenMatcher = java.util.regex.Pattern
            .compile("\\\"accessToken\\\":\\\"([^\\\"]+)\\\"").matcher(response);
        if (!tokenMatcher.find()) {
            throw new IllegalStateException("Login response did not contain an access token");
        }
        return tokenMatcher.group(1);
    }

    private long responseId(String response, String field) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\\"" + field + "\\\":(\\d+)")
                .matcher(response);
        if (!matcher.find()) {
            throw new IllegalStateException("Response did not contain " + field);
        }
        return Long.parseLong(matcher.group(1));
    }

    private String responseString(String response, String field) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\\"" + field + "\\\":\\\"([^\\\"]+)\\\"")
                .matcher(response);
        if (!matcher.find()) {
            throw new IllegalStateException("Response did not contain " + field);
        }
        return matcher.group(1);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}