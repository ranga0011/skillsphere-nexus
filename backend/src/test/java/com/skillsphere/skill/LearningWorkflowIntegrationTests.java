package com.skillsphere.skill;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jayway.jsonpath.JsonPath;
import com.skillsphere.skill.entity.CompetencyFramework;
import com.skillsphere.skill.entity.FrameworkSkillRequirement;
import com.skillsphere.skill.entity.Student;
import com.skillsphere.skill.entity.Skill;
import com.skillsphere.skill.repository.AssessmentDefinitionRepository;
import com.skillsphere.skill.repository.AssessmentQuestionRepository;
import com.skillsphere.skill.repository.IssuedCertificateRepository;
import com.skillsphere.skill.repository.LearnerAssessmentAttemptRepository;
import com.skillsphere.skill.repository.SkillRepository;
import com.skillsphere.skill.repository.StudentRepository;
import com.skillsphere.skill.repository.CompetencyFrameworkRepository;
import com.skillsphere.skill.repository.FrameworkSkillRequirementRepository;
import com.skillsphere.skill.security.AccountStatusAuditRepository;
import com.skillsphere.skill.security.AccountAdministrationService;
import com.skillsphere.skill.security.AppUser;
import com.skillsphere.skill.security.AppUserRepository;
import com.skillsphere.skill.security.UserRole;
import com.skillsphere.skill.service.QuestionBankService;
import com.skillsphere.skill.service.LearningModels.AssessmentRequest;
import com.skillsphere.skill.service.LearningModels.QuestionRequest;
import com.skillsphere.skill.entity.AssessmentDifficulty;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LearningWorkflowIntegrationTests extends IsolatedIntegrationTest {
    private static final String ADMIN_PASSWORD = "Workflow-Admin-Password-2026";

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtEncoder jwtEncoder;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AppUserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private SkillRepository skillRepository;
    @Autowired private AssessmentDefinitionRepository assessmentRepository;
    @Autowired private AssessmentQuestionRepository questionRepository;
    @Autowired private LearnerAssessmentAttemptRepository attemptRepository;
    @Autowired private IssuedCertificateRepository certificateRepository;
    @Autowired private CompetencyFrameworkRepository frameworkRepository;
    @Autowired private FrameworkSkillRequirementRepository requirementRepository;
    @Autowired private QuestionBankService questionBankService;
    @Autowired private AccountStatusAuditRepository accountStatusAuditRepository;
    @Autowired private AccountAdministrationService accountAdministrationService;

    private Student student;
    private Skill skill;
    private String adminToken;
    private String studentToken;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        AppUser admin = userRepository.save(new AppUser("workflow-admin@example.test",
                passwordEncoder.encode(ADMIN_PASSWORD), UserRole.HR_ADMIN, null));
        student = studentRepository.save(new Student("Workflow Learner", "workflow-learner@example.test",
                "Software Engineering"));
        userRepository.save(new AppUser("workflow-learner@example.test",
                passwordEncoder.encode("Workflow-Student-Password-2026"), UserRole.STUDENT,
                null, student.getId()));
        skill = skillRepository.save(new Skill("Workflow Java", "Technical", "Assessment test skill"));
        adminToken = createToken(admin);
        studentToken = createToken(userRepository.findByUsername("workflow-learner@example.test").orElseThrow());
    }

    @Test
    void learnerAssessmentIsServerScoredAndIssuesCertificateOnce() throws Exception {
        String assessmentResponse = mockMvc.perform(post("/api/admin/learning/assessments")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Java Fundamentals","instructions":"Choose the best answer.",
                                 "skillId":%d,"passPercentage":80,"maxAttempts":1,"active":true}
                                """.formatted(skill.getId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number assessmentIdValue = JsonPath.read(assessmentResponse, "$.id");
        long assessmentId = assessmentIdValue.longValue();

        mockMvc.perform(post("/api/admin/learning/assessments/{id}/questions", assessmentId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"prompt":"Which is a Java keyword?","options":["apply","class","packagee","run"],
                                 "correctOptionIndex":1,"difficulty":"BEGINNER","points":1}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/admin/learning/assessments/{id}/questions", assessmentId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"prompt":"Which type is integral?","options":["String","boolean","int","Object"],
                                 "correctOptionIndex":2,"difficulty":"INTERMEDIATE","points":1}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/learning/assessments/{id}/questions", assessmentId)
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        String startResponse = mockMvc.perform(post("/api/learning/assessments/{id}/attempts", assessmentId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[0].correctOptionIndex").doesNotExist())
                .andExpect(jsonPath("$.startedAt").exists())
                .andReturn().getResponse().getContentAsString();
        Number attemptIdValue = JsonPath.read(startResponse, "$.attemptId");
        long attemptId = attemptIdValue.longValue();
        mockMvc.perform(delete("/api/admin/learning/assessments/{id}/questions/{questionId}",
                        assessmentId, questionRepository.findByAssessment_IdOrderById(assessmentId).get(0).getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isConflict());
        var questions = questionRepository.findByAssessment_IdOrderById(assessmentId);
        String submitBody = """
                {"answers":[{"questionId":%d,"selectedOptionIndex":1},
                            {"questionId":%d,"selectedOptionIndex":2}]}
                """.formatted(questions.get(0).getId(), questions.get(1).getId());
        String result = mockMvc.perform(post("/api/learning/attempts/{id}/submit", attemptId)
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.percentage").value(100))
                .andExpect(jsonPath("$.certificate.certificateNumber").exists())
                .andReturn().getResponse().getContentAsString();
        Number certificateIdValue = JsonPath.read(result, "$.certificate.id");
        long certificateId = certificateIdValue.longValue();
        org.junit.jupiter.api.Assertions.assertEquals(1, certificateRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(1, certificateRepository.findAll().size());

        mockMvc.perform(post("/api/learning/attempts/{id}/submit", attemptId)
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/certificates")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(certificateId));
        mockMvc.perform(post("/api/learning/assessments/{id}/attempts", assessmentId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isConflict());
        org.junit.jupiter.api.Assertions.assertEquals(1, attemptRepository.count());
    }

    @Test
    void failedAssessmentDoesNotIssueCertificateAndAttemptIsOwnerScoped() throws Exception {
        AssessmentRequest request = new AssessmentRequest("Failing Assessment", "Instructions",
                skill.getId(), 80, 2, true);
        var assessment = questionBankService.createAssessment(request);
        questionBankService.addQuestion(assessment.id(),
                new QuestionRequest("One?", java.util.List.of("A", "B"), 0, AssessmentDifficulty.BEGINNER, 1));
        questionBankService.addQuestion(assessment.id(),
                new QuestionRequest("Two?", java.util.List.of("A", "B"), 0, AssessmentDifficulty.BEGINNER, 1));
        String start = mockMvc.perform(post("/api/learning/assessments/{id}/attempts", assessment.id())
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Number attemptIdValue = JsonPath.read(start, "$.attemptId");
        long attemptId = attemptIdValue.longValue();
        var questions = questionRepository.findByAssessment_IdOrderById(assessment.id());
        String answers = """
                {"answers":[{"questionId":%d,"selectedOptionIndex":0},
                            {"questionId":%d,"selectedOptionIndex":1}]}
                """.formatted(questions.get(0).getId(), questions.get(1).getId());

        Student other = studentRepository.save(new Student("Other Learner", "other-workflow@example.test",
                "Software Engineering"));
        AppUser otherUser = userRepository.save(new AppUser("other-workflow@example.test",
                passwordEncoder.encode("Workflow-Other-Password-2026"), UserRole.STUDENT, null, other.getId()));
        String otherToken = createToken(otherUser);
        mockMvc.perform(post("/api/learning/attempts/{id}/submit", attemptId)
                        .header("Authorization", bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON).content(answers))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/learning/attempts/{id}/submit", attemptId)
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON).content(answers))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passed").value(false))
                .andExpect(jsonPath("$.certificate").doesNotExist());
        org.junit.jupiter.api.Assertions.assertEquals(0, certificateRepository.count());
    }

    @Test
    void deactivationInvalidatesExistingTokensAndLastAdminCannotBeDisabled() throws Exception {
        long adminId = userRepository.findByUsername("workflow-admin@example.test").orElseThrow().getId();
        long studentUserId = userRepository.findByUsername("workflow-learner@example.test").orElseThrow().getId();
        mockMvc.perform(put("/api/admin/users/{id}/status", adminId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/admin/users/{id}/status", studentUserId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.enabled").value(false));
        mockMvc.perform(get("/api/students/me").header("Authorization", bearer(studentToken)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"workflow-learner@example.test",
                                 "password":"Workflow-Student-Password-2026"}
                                """))
                .andExpect(status().isUnauthorized());
        org.junit.jupiter.api.Assertions.assertEquals(1, accountStatusAuditRepository.count());
    }

    @Test
    void lastActiveAdministratorCannotBeDisabledByASeparateActor() {
        long adminId = userRepository.findByUsername("workflow-admin@example.test").orElseThrow().getId();
        org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
                () -> accountAdministrationService.updateStatus(adminId, false, "another-actor@example.test"));
    }

    @Test
    void analyticsAreAdminOnlyAndResourceRecommendationsFollowStoredGaps() throws Exception {
        CompetencyFramework framework = frameworkRepository.save(
                new CompetencyFramework("Backend Engineer", "Backend role", "Backend Engineer"));
        FrameworkSkillRequirement requirement = new FrameworkSkillRequirement();
        requirement.setFramework(framework);
        requirement.setSkill(skill);
        requirement.setRequiredProficiency(7);
        requirementRepository.save(requirement);
        String resourcePayload = """
                {"title":"Java reference notes","description":"Configured admin resource",
                 "skillId":%d,"resourceType":"Article","difficultyLevel":"Beginner",
                 "verifiedUrl":"https://example.org/java-notes","active":true}
                """.formatted(skill.getId());
        mockMvc.perform(post("/api/admin/learning/resources")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(resourcePayload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/analytics/skill-gaps").param("frameworkId", framework.getId().toString())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLearners").value(1))
                .andExpect(jsonPath("$.skills[0].learnersWithGap").value(1))
                .andExpect(jsonPath("$.skills[0].awaitingAssessment").value(1));
        mockMvc.perform(get("/api/admin/analytics/skill-gaps").param("frameworkId", framework.getId().toString())
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/learning/resources/recommendations")
                        .param("frameworkId", framework.getId().toString())
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resource.skillId").value(skill.getId()))
                .andExpect(jsonPath("$[0].priorityGap").value(7));
    }

    private String createToken(AppUser user) {
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer("skillsphere-nexus")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(900))
                .subject(user.getUsername())
                .claim("role", user.getRole().name());
        if (user.getStudentId() != null) builder.claim("studentId", user.getStudentId());
        if (user.getEmployeeId() != null) builder.claim("employeeId", user.getEmployeeId());
        JwtClaimsSet claims = builder.build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
