package com.skillsphere.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.skillsphere.skill.entity.Employee;
import com.skillsphere.skill.entity.Skill;
import com.skillsphere.skill.entity.CompetencyGap;
import com.skillsphere.skill.entity.CertificationStatus;
import com.skillsphere.skill.entity.VerificationStatus;
import com.skillsphere.skill.repository.CertificationRepository;
import com.skillsphere.skill.repository.CompetencyFrameworkRepository;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.repository.EmployeeSkillMappingRepository;
import com.skillsphere.skill.repository.SkillAssessmentRepository;
import com.skillsphere.skill.repository.SkillRepository;
import com.skillsphere.skill.repository.IssuedCertificateRepository;
import com.skillsphere.skill.service.CompetencyFrameworkService;
import com.skillsphere.skill.service.CertificationService;
import com.skillsphere.skill.service.EmployeeSkillMappingService;
import com.skillsphere.skill.service.SkillAssessmentService;
import com.skillsphere.skill.entity.IssuedCertificateStatus;
import com.skillsphere.skill.service.exception.DuplicateResourceException;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "HR_ADMIN")
class Milestone1FeatureTests extends IsolatedIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @PersistenceContext
        private EntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private EmployeeSkillMappingRepository employeeSkillMappingRepository;

    @Autowired
    private SkillAssessmentRepository skillAssessmentRepository;

    @Autowired
    private CompetencyFrameworkRepository competencyFrameworkRepository;

    @Autowired
    private CertificationRepository certificationRepository;

    @Autowired
    private IssuedCertificateRepository issuedCertificateRepository;

    @Autowired
    private EmployeeSkillMappingService employeeSkillMappingService;

    @Autowired
    private SkillAssessmentService skillAssessmentService;

    @Autowired
    private CompetencyFrameworkService competencyFrameworkService;

    @Autowired
    private CertificationService certificationService;

    private Employee employee;
    private Skill skill;

    @BeforeEach
        @SuppressWarnings("unused")
    void setUp() {
        employee = employeeRepository.save(new Employee("Test User", "test.user@example.com", "Engineer", "Engineering"));
        skill = skillRepository.save(new Skill("Spring Boot", "Technical", "Backend framework"));
    }

    @Test
    void employeeSkillMapping_enforcesDuplicateAndProficiencyRules() {
        var mapping = employeeSkillMappingService.assignSkill(employee.getId(), skill.getId(), 8, VerificationStatus.VERIFIED, LocalDate.now());
        assertNotNull(mapping.getId());
        entityManager.flush();
        entityManager.clear();
        assertEquals(1, employeeSkillMappingRepository.findByEmployee_Id(employee.getId()).size());

        var duplicate = assertThrows(DuplicateResourceException.class,
                () -> employeeSkillMappingService.assignSkill(employee.getId(), skill.getId(), 7, VerificationStatus.PENDING_VERIFICATION, LocalDate.now()));
        assertEquals("Employee already has this skill assigned", duplicate.getMessage());

        var missingEmployee = assertThrows(ResourceNotFoundException.class,
                () -> employeeSkillMappingService.assignSkill(999L, skill.getId(), 6, VerificationStatus.VERIFIED, LocalDate.now()));
        assertEquals("Employee not found with id: 999", missingEmployee.getMessage());

        assertNotNull(assertThrows(ResourceNotFoundException.class,
                () -> employeeSkillMappingService.assignSkill(employee.getId(), 999L, 6, VerificationStatus.VERIFIED, LocalDate.now())));

        var tooLow = assertThrows(IllegalArgumentException.class,
                () -> employeeSkillMappingService.assignSkill(employee.getId(), skill.getId(), 0, VerificationStatus.VERIFIED, LocalDate.now()));
        assertEquals("Proficiency must be between 1 and 10", tooLow.getMessage());

        var tooHigh = assertThrows(IllegalArgumentException.class,
                () -> employeeSkillMappingService.assignSkill(employee.getId(), skill.getId(), 11, VerificationStatus.VERIFIED, LocalDate.now()));
        assertEquals("Proficiency must be between 1 and 10", tooHigh.getMessage());

        Skill secondSkill = skillRepository.save(new Skill("Kotlin", "Technical", "JVM language"));
        var secondMapping = employeeSkillMappingService.assignSkill(employee.getId(), secondSkill.getId(), 5, VerificationStatus.PENDING_VERIFICATION, LocalDate.now());
        assertNotNull(assertThrows(DuplicateResourceException.class,
                () -> employeeSkillMappingService.updateMapping(secondMapping.getId(), employee.getId(), skill.getId(), 6, null, LocalDate.now())));
    }

    @Test
    void skillAssessment_validatesScoreAndDefaultVerificationStatus() {
        var assessment = skillAssessmentService.createAssessment(
                employee.getId(),
                skill.getId(),
                85,
                100,
                "Practical",
                "Jane Manager",
                LocalDate.now());

        assertNotNull(assessment.getId());
        assertEquals(VerificationStatus.PENDING_VERIFICATION, assessment.getVerificationStatus());
        assertEquals(VerificationStatus.VERIFIED,
                skillAssessmentService.verifyAssessment(assessment.getId(), VerificationStatus.VERIFIED).getVerificationStatus());
        skillAssessmentService.createAssessment(
                employee.getId(), skill.getId(), 92, 100, "Practical", "Jane Manager",
                LocalDate.now().minusDays(1));
        entityManager.flush();
        entityManager.clear();
        assertEquals(2, skillAssessmentRepository.findByEmployee_Id(employee.getId()).size());
        assertEquals(2, skillAssessmentService.filterAssessments(employee.getId(), null, null, null, null).size());

        var invalidScore = assertThrows(IllegalArgumentException.class,
                () -> skillAssessmentService.createAssessment(
                        employee.getId(),
                        skill.getId(),
                        120,
                        100,
                        "Practical",
                        "Jane Manager",
                        LocalDate.now()));
        assertEquals("Score must be between 0 and the maximum score", invalidScore.getMessage());
        assertNotNull(assertThrows(ResourceNotFoundException.class,
                () -> skillAssessmentService.createAssessment(999L, skill.getId(), 1, 10, "Quiz", null,
                        LocalDate.now())));
        assertNotNull(assertThrows(IllegalArgumentException.class,
                () -> skillAssessmentService.filterAssessments(null, null, null, LocalDate.now(), LocalDate.now().minusDays(1))));
    }

    @Test
    void competencyFramework_calculatesSkillGaps() {
        var framework = competencyFrameworkService.createFramework("Platform Engineer", "Backend role requirements");
        competencyFrameworkService.addRequirement(framework.getId(), skill.getId(), 7, "Java proficiency required");
        employeeSkillMappingService.assignSkill(employee.getId(), skill.getId(), 4, VerificationStatus.VERIFIED, LocalDate.now());

        Skill matchedSkill = skillRepository.save(new Skill("PostgreSQL", "Technical", "Relational database"));
        competencyFrameworkService.addRequirement(framework.getId(), matchedSkill.getId(), 7, "Database proficiency required");
        employeeSkillMappingService.assignSkill(employee.getId(), matchedSkill.getId(), 8, VerificationStatus.VERIFIED, LocalDate.now());

        Skill missingSkill = skillRepository.save(new Skill("Kubernetes", "Technical", "Container orchestration"));
        competencyFrameworkService.addRequirement(framework.getId(), missingSkill.getId(), 6, "Orchestration proficiency required");

        List<CompetencyGap> gaps = competencyFrameworkService.getGapReport(framework.getId(), employee.getId());

        assertEquals(3, gaps.size());
        assertEquals(3, gaps.get(0).getGap());
        assertEquals("UNDER_REQUIRED", gaps.get(0).getStatus());
        assertEquals("MATCHED", gaps.get(1).getStatus());
        assertEquals(0, gaps.get(1).getGap());
        assertEquals("NOT_ASSIGNED", gaps.get(2).getStatus());
        assertEquals(6, gaps.get(2).getGap());

        Employee unassigned = employeeRepository.save(new Employee("Unassigned User", "unassigned@example.com", "Engineer", "Engineering"));
        List<CompetencyGap> unassignedGaps = competencyFrameworkService.getGapReport(framework.getId(), unassigned.getId());
        assertEquals(3, unassignedGaps.size());
        assertEquals(List.of("NOT_ASSIGNED", "NOT_ASSIGNED", "NOT_ASSIGNED"),
                unassignedGaps.stream().map(gap -> gap.getStatus()).toList());

        var emptyFramework = competencyFrameworkService.createFramework("Empty Framework", "No requirements");
        assertEquals(List.of(), competencyFrameworkService.getGapReport(emptyFramework.getId(), unassigned.getId()));
        assertNotNull(assertThrows(IllegalArgumentException.class,
                () -> competencyFrameworkService.addRequirement(framework.getId(), skill.getId(), 0, "Invalid")));
        assertNotNull(competencyFrameworkRepository.findById(framework.getId()).orElse(null));
    }

    @Test
    void gapAnalysisDoesNotCountUnverifiedSkillsAndComputesReadiness() {
        var framework = competencyFrameworkService.createFramework("Verified role", "Evidence-aware report");
        competencyFrameworkService.addRequirement(framework.getId(), skill.getId(), 8, "Required");
        employeeSkillMappingService.assignSkill(employee.getId(), skill.getId(), 8,
                VerificationStatus.PENDING_VERIFICATION, LocalDate.now());

        CompetencyGap gap = competencyFrameworkService.getGapReport(framework.getId(), employee.getId()).getFirst();
        assertEquals("UNVERIFIED", gap.getStatus());
        assertNull(gap.getEmployeeProficiency());
        assertEquals(8, gap.getGap());
        assertEquals("PENDING_VERIFICATION", gap.getEvidenceStatus());
        assertEquals(0.0, competencyFrameworkService.getEmployeeGapSummary(framework.getId(), employee.getId())
                .readinessPercentage());

        employeeSkillMappingService.updateMapping(
                employeeSkillMappingRepository.findByEmployee_Id(employee.getId()).getFirst().getId(),
                null, null, null, VerificationStatus.VERIFIED, null);
        var summary = competencyFrameworkService.getEmployeeGapSummary(framework.getId(), employee.getId());
        assertEquals(100.0, summary.readinessPercentage());
        assertEquals(1, summary.satisfiedRequirements());
    }

    @Test
    void verifiedPassingAssessmentIssuesOneValidPdfCertificate() throws Exception {
        var assessment = skillAssessmentService.createAssessment(
                employee.getId(), skill.getId(), 85, 100, "Practical", "Authorized assessor", LocalDate.now());
        assertEquals(0, issuedCertificateRepository.findByEmployee_Id(employee.getId()).size());

        skillAssessmentService.verifyAssessment(assessment.getId(), VerificationStatus.VERIFIED);
        var issued = issuedCertificateRepository.findByEmployee_Id(employee.getId());
        assertEquals(1, issued.size());
        assertEquals(IssuedCertificateStatus.ISSUED, issued.getFirst().getStatus());
        assertTrue(issued.getFirst().getCertificateNumber().startsWith("SSN-"));

        byte[] pdf = issued.getFirst().getPdfContent();
        try (var document = org.apache.pdfbox.Loader.loadPDF(pdf)) {
            String text = new org.apache.pdfbox.text.PDFTextStripper().getText(document);
            assertTrue(text.contains(employee.getName()));
            assertTrue(text.contains(skill.getName()));
            assertTrue(text.contains(issued.getFirst().getCertificateNumber()));
            assertTrue(text.contains(issued.getFirst().getVerificationToken()));
        }

        skillAssessmentService.verifyAssessment(assessment.getId(), VerificationStatus.VERIFIED);
        assertEquals(1, issuedCertificateRepository.findByEmployee_Id(employee.getId()).size());
        assertNotNull(assertThrows(DuplicateResourceException.class,
                () -> skillAssessmentService.deleteAssessment(assessment.getId())));
        skillAssessmentService.verifyAssessment(assessment.getId(), VerificationStatus.REJECTED, "reviewer@example.com");
        var revoked = issuedCertificateRepository.findByEmployee_Id(employee.getId()).getFirst();
        assertEquals(IssuedCertificateStatus.REVOKED, revoked.getStatus());
        assertEquals("reviewer@example.com", revoked.getRevokedBy());
        assertNotNull(revoked.getRevokedAt());
        assertEquals(VerificationStatus.PENDING_VERIFICATION,
                employeeSkillMappingRepository.findByEmployee_Id(employee.getId()).getFirst().getVerificationStatus());
    }

    @Test
    void rejectingVerifiedAssessmentRecalculatesProficiencyFromRemainingEvidence() {
        var framework = competencyFrameworkService.createFramework("Evidence role", "Reverification behavior");
        competencyFrameworkService.addRequirement(framework.getId(), skill.getId(), 10, "Required");
        var first = skillAssessmentService.createAssessment(
                employee.getId(), skill.getId(), 100, 100, "Practical", "Assessor", LocalDate.now().minusDays(1));
        var second = skillAssessmentService.createAssessment(
                employee.getId(), skill.getId(), 40, 100, "Practical", "Assessor", LocalDate.now());

        skillAssessmentService.verifyAssessment(first.getId(), VerificationStatus.VERIFIED);
        skillAssessmentService.verifyAssessment(second.getId(), VerificationStatus.VERIFIED);
        assertEquals(4, employeeSkillMappingRepository.findByEmployee_Id(employee.getId()).getFirst().getProficiency());

        skillAssessmentService.verifyAssessment(second.getId(), VerificationStatus.REJECTED);
        var fallback = employeeSkillMappingRepository.findByEmployee_Id(employee.getId()).getFirst();
        assertEquals(10, fallback.getProficiency());
        assertEquals(VerificationStatus.VERIFIED, fallback.getVerificationStatus());

        skillAssessmentService.verifyAssessment(first.getId(), VerificationStatus.REJECTED);
        var unverified = employeeSkillMappingRepository.findByEmployee_Id(employee.getId()).getFirst();
        assertEquals(VerificationStatus.PENDING_VERIFICATION, unverified.getVerificationStatus());
        assertEquals(0.0, competencyFrameworkService.getEmployeeGapSummary(framework.getId(), employee.getId())
                .readinessPercentage());
    }

    @Test
    void failedAssessmentDoesNotIssueCertificate() {
        var assessment = skillAssessmentService.createAssessment(
                employee.getId(), skill.getId(), 69, 100, "Practical", "Authorized assessor", LocalDate.now());
        skillAssessmentService.verifyAssessment(assessment.getId(), VerificationStatus.VERIFIED);
        assertEquals(0, issuedCertificateRepository.findByEmployee_Id(employee.getId()).size());
    }

    @Test
    void certification_statuses_calculateFromDates() {
        var active = certificationService.createCertification(
                employee.getId(),
                "AWS Cloud Practitioner",
                "AWS",
                "CERT-001",
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(6),
                VerificationStatus.PENDING_VERIFICATION);

        var expired = certificationService.createCertification(
                employee.getId(),
                "Legacy Java",
                "Oracle",
                "CERT-002",
                LocalDate.now().minusYears(2),
                LocalDate.now().minusDays(5),
                VerificationStatus.VERIFIED);

        var noExpiry = certificationService.createCertification(
                employee.getId(),
                "Internal Training",
                "SkillSphere",
                "CERT-003",
                LocalDate.now().minusDays(10),
                null,
                VerificationStatus.PENDING_VERIFICATION);

        var expiringSoon = certificationService.createCertification(
                employee.getId(), "Renewal Due", "Example Org", "CERT-004",
                LocalDate.now().minusYears(1), LocalDate.now().plusDays(30), VerificationStatus.PENDING_VERIFICATION);

        assertEquals(CertificationStatus.ACTIVE, active.getStatus());
        assertEquals(CertificationStatus.EXPIRED, expired.getStatus());
        assertEquals(CertificationStatus.ACTIVE, noExpiry.getStatus());
        assertEquals(VerificationStatus.PENDING_VERIFICATION, noExpiry.getVerificationStatus());
        assertEquals(CertificationStatus.EXPIRING_SOON, expiringSoon.getStatus());
        assertEquals(VerificationStatus.PENDING_VERIFICATION, expiringSoon.getVerificationStatus());
        assertEquals(4, certificationRepository.findByEmployee_Id(employee.getId()).size());

        assertNotNull(assertThrows(IllegalArgumentException.class,
                () -> certificationService.createCertification(employee.getId(), "Invalid Dates", "Example Org", null,
                        LocalDate.now(), LocalDate.now().minusDays(1), VerificationStatus.VERIFIED)));
        assertNotNull(assertThrows(ResourceNotFoundException.class,
                () -> certificationService.createCertification(999L, "Unknown Employee", "Example Org", null,
                        LocalDate.now(), null, VerificationStatus.PENDING_VERIFICATION)));

        certificationService.updateCertification(noExpiry.getId(), employee.getId(), null, null, null,
                null, null, null);
        assertEquals(CertificationStatus.ACTIVE, certificationService.getCertificationById(noExpiry.getId()).getStatus());
        assertNull(certificationService.getCertificationById(noExpiry.getId()).getExpiryDate());
    }

    @Test
    void mappingEndpoint_acceptsScalarIdsAndReturnsValidationStatus() throws Exception {
        String validPayload = """
                {"employeeId":%d,"skillId":%d,"proficiency":7,"verificationStatus":"PENDING_VERIFICATION","assessmentDate":"%s"}
                """.formatted(employee.getId(), skill.getId(), LocalDate.now());

        mockMvc.perform(post("/api/employee-skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.proficiency").value(7))
                .andExpect(jsonPath("$.employeeId").value(employee.getId()))
                .andExpect(jsonPath("$.skillId").value(skill.getId()));

        mockMvc.perform(get("/api/employee-skills/employee/{employeeId}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee.name").value(employee.getName()))
                .andExpect(jsonPath("$[0].skill.name").value(skill.getName()));

        String invalidPayload = """
                {"employeeId":%d,"skillId":%d,"proficiency":11,"verificationStatus":"PENDING_VERIFICATION"}
                """.formatted(employee.getId(), skill.getId());

        mockMvc.perform(post("/api/employee-skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Proficiency must be between 1 and 10"));

        String missingEmployeePayload = """
                {"employeeId":99999999,"skillId":%d,"proficiency":7,"verificationStatus":"PENDING_VERIFICATION"}
                """.formatted(skill.getId());

        mockMvc.perform(post("/api/employee-skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingEmployeePayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found with id: 99999999"));
    }

    @Test
    void employeeAndSkillCatalogCrudRemainsAvailableToHr() throws Exception {
        String employeeResponse = mockMvc.perform(post("/api/employees")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"CRUD Employee","email":"crud.employee@example.test",
                                 "jobTitle":"Engineer","department":"Engineering"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("CRUD Employee"))
                .andReturn().getResponse().getContentAsString();
        long employeeId = ((Number) JsonPath.read(employeeResponse, "$.id")).longValue();

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated CRUD Employee","email":"crud.employee@example.test",
                                 "jobTitle":"Senior Engineer","department":"Platform"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated CRUD Employee"));
        mockMvc.perform(get("/api/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("Platform"));
        mockMvc.perform(delete("/api/employees/{id}", employeeId))
                .andExpect(status().isNoContent());

        String skillResponse = mockMvc.perform(post("/api/skills")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"CRUD Skill","category":"Technical","description":"Original"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long skillId = ((Number) JsonPath.read(skillResponse, "$.id")).longValue();

        mockMvc.perform(put("/api/skills/{id}", skillId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated CRUD Skill","category":"Technical","description":"Updated"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated CRUD Skill"));
        mockMvc.perform(get("/api/skills/{id}", skillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"));
        mockMvc.perform(delete("/api/skills/{id}", skillId))
                .andExpect(status().isNoContent());
    }

    @Test
    void assessmentVerification_requiresAnExplicitAction() throws Exception {
        String payload = """
                {"employeeId":%d,"skillId":%d,"score":85,"maxScore":100,"assessmentMethod":"Practical","assessorName":"explicit-verification-api-test","assessmentDate":"%s","verificationStatus":"VERIFIED"}
                """.formatted(employee.getId(), skill.getId(), LocalDate.now());

        mockMvc.perform(post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verificationStatus").value("PENDING_VERIFICATION"));
        long assessmentId = skillAssessmentRepository.findAll().stream()
                .filter(item -> "explicit-verification-api-test".equals(item.getAssessorName()))
                .map(item -> item.getId())
                .findFirst()
                .orElseThrow();

        mockMvc.perform(put("/api/assessments/{id}/verification", assessmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verificationStatus\":\"VERIFIED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));

        mockMvc.perform(put("/api/assessments/{id}/verification", assessmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verificationStatus\":\"PENDING_VERIFICATION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Verification action must set VERIFIED or REJECTED"));

        mockMvc.perform(put("/api/assessments/{id}/verification", assessmentId)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("VERIFIED"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("Content-Type must be application/json"));
    }
}
