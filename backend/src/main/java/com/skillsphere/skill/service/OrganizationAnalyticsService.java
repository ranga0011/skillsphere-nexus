package com.skillsphere.skill.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.CompetencyFramework;
import com.skillsphere.skill.entity.Employee;
import com.skillsphere.skill.entity.EmployeeSkillMapping;
import com.skillsphere.skill.entity.FrameworkSkillRequirement;
import com.skillsphere.skill.entity.Student;
import com.skillsphere.skill.entity.VerificationStatus;
import com.skillsphere.skill.repository.CertificationRepository;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.repository.EmployeeSkillMappingRepository;
import com.skillsphere.skill.repository.StudentRepository;
import com.skillsphere.skill.service.LearningModels.CertificateExpirySummary;
import com.skillsphere.skill.service.LearningModels.GapSkillAnalytics;
import com.skillsphere.skill.service.LearningModels.OrganizationGapAnalytics;

@Service
@Transactional(readOnly = true)
public class OrganizationAnalyticsService {
    private final CompetencyFrameworkService frameworkService;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;
    private final EmployeeSkillMappingRepository mappingRepository;
    private final CertificationRepository certificationRepository;

    public OrganizationAnalyticsService(CompetencyFrameworkService frameworkService,
            EmployeeRepository employeeRepository, StudentRepository studentRepository,
            EmployeeSkillMappingRepository mappingRepository, CertificationRepository certificationRepository) {
        this.frameworkService = frameworkService;
        this.employeeRepository = employeeRepository;
        this.studentRepository = studentRepository;
        this.mappingRepository = mappingRepository;
        this.certificationRepository = certificationRepository;
    }

    public OrganizationGapAnalytics getFrameworkAnalytics(Long frameworkId, int page, int size, String skillFilter) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
        CompetencyFramework framework = frameworkService.getFrameworkById(frameworkId);
        List<FrameworkSkillRequirement> requirements = frameworkService.getRequirementsByFramework(frameworkId);
        List<Employee> employees = employeeRepository.findAll();
        List<Student> students = studentRepository.findAll();
        List<EmployeeSkillMapping> mappings = mappingRepository.findAll();
        int learnerCount = employees.size() + students.size();
        Map<String, EmployeeSkillMapping> learnerSkills = new HashMap<>();
        for (EmployeeSkillMapping mapping : mappings) {
            if (mapping.getSkillId() == null) {
                continue;
            }
            Long profileId = mapping.getStudentId() != null ? mapping.getStudentId() : mapping.getEmployeeId();
            String role = mapping.getStudentId() != null ? "S:" : "E:";
            if (profileId != null) {
                learnerSkills.put(role + profileId + ":" + mapping.getSkillId(), mapping);
            }
        }
        List<GapSkillAnalytics> analytics = new ArrayList<>();
        int meetingAll = 0;
        for (Employee employee : employees) {
            if (meetsAll(requirements, learnerSkills, "E:" + employee.getId() + ":")) {
                meetingAll++;
            }
        }
        for (Student student : students) {
            if (meetsAll(requirements, learnerSkills, "S:" + student.getId() + ":")) {
                meetingAll++;
            }
        }
        for (FrameworkSkillRequirement requirement : requirements) {
            int learnersWithGap = 0;
            int totalGapLevels = 0;
            int awaitingAssessment = 0;
            int awaitingVerification = 0;
            int required = requirement.getRequiredProficiency();
            for (Employee employee : employees) {
                EmployeeSkillMapping mapping = learnerSkills.get("E:" + employee.getId() + ":" + requirement.getSkillId());
                int current = getVerifiedProficiency(mapping);
                int gap = Math.max(0, required - current);
                if (gap > 0) learnersWithGap++;
                totalGapLevels += gap;
                if (mapping == null) awaitingAssessment++;
                else if (mapping.getVerificationStatus() != VerificationStatus.VERIFIED) awaitingVerification++;
            }
            for (Student student : students) {
                EmployeeSkillMapping mapping = learnerSkills.get("S:" + student.getId() + ":" + requirement.getSkillId());
                int current = getVerifiedProficiency(mapping);
                int gap = Math.max(0, required - current);
                if (gap > 0) learnersWithGap++;
                totalGapLevels += gap;
                if (mapping == null) awaitingAssessment++;
                else if (mapping.getVerificationStatus() != VerificationStatus.VERIFIED) awaitingVerification++;
            }
            analytics.add(new GapSkillAnalytics(requirement.getSkillId(),
                    requirement.getSkill() == null ? "Unknown skill" : requirement.getSkill().getName(),
                    required, learnersWithGap,
                    learnerCount == 0 ? 0.0 : learnersWithGap * 100.0 / learnerCount,
                    totalGapLevels, awaitingAssessment, awaitingVerification));
        }
        if (skillFilter != null && !skillFilter.isBlank()) {
            String normalizedFilter = skillFilter.trim().toLowerCase(java.util.Locale.ROOT);
            analytics.removeIf(skill -> !skill.skillName().toLowerCase(java.util.Locale.ROOT)
                    .contains(normalizedFilter));
        }
        analytics.sort(Comparator.comparingInt(GapSkillAnalytics::learnersWithGap).reversed()
                .thenComparing(Comparator.comparingInt(GapSkillAnalytics::totalGapLevels).reversed())
                .thenComparing(GapSkillAnalytics::skillName));
        int totalSkills = analytics.size();
        int fromIndex = Math.min(page * size, totalSkills);
        int toIndex = Math.min(fromIndex + size, totalSkills);
        List<GapSkillAnalytics> pageItems = List.copyOf(analytics.subList(fromIndex, toIndex));
        return new OrganizationGapAnalytics(framework.getId(), framework.getName(), learnerCount, meetingAll,
                learnerCount == 0 ? 0.0 : meetingAll * 100.0 / learnerCount, requirements.size(),
                certificateExpiry(), pageItems, totalSkills, page, size);
    }

    private boolean meetsAll(List<FrameworkSkillRequirement> requirements,
            Map<String, EmployeeSkillMapping> learnerSkills, String learnerPrefix) {
        return requirements.stream().allMatch(requirement -> {
            EmployeeSkillMapping mapping = learnerSkills.get(learnerPrefix + requirement.getSkillId());
            return mapping != null && mapping.getVerificationStatus() == VerificationStatus.VERIFIED
                    && mapping.getProficiency() != null
                    && mapping.getProficiency() >= requirement.getRequiredProficiency();
        });
    }

    private int getVerifiedProficiency(EmployeeSkillMapping mapping) {
        return mapping != null && mapping.getVerificationStatus() == VerificationStatus.VERIFIED
                && mapping.getProficiency() != null ? mapping.getProficiency() : 0;
    }

    private CertificateExpirySummary certificateExpiry() {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(30);
        long active = 0;
        long expiring = 0;
        long expired = 0;
        for (var certification : certificationRepository.findAll()) {
            LocalDate expiry = certification.getExpiryDate();
            if (expiry == null) {
                active++;
            } else if (expiry.isBefore(today)) {
                expired++;
            } else if (!expiry.isAfter(cutoff)) {
                expiring++;
                active++;
            } else {
                active++;
            }
        }
        return new CertificateExpirySummary(active, expiring, expired);
    }
}
