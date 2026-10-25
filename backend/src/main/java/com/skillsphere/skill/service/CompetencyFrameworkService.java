package com.skillsphere.skill.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.CompetencyFramework;
import com.skillsphere.skill.entity.CompetencyGap;
import com.skillsphere.skill.entity.CompetencyGapReport;
import com.skillsphere.skill.entity.EmployeeSkillMapping;
import com.skillsphere.skill.entity.FrameworkSkillRequirement;
import com.skillsphere.skill.entity.Skill;
import com.skillsphere.skill.repository.CompetencyFrameworkRepository;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.repository.EmployeeSkillMappingRepository;
import com.skillsphere.skill.repository.FrameworkSkillRequirementRepository;
import com.skillsphere.skill.repository.SkillRepository;
import com.skillsphere.skill.repository.StudentRepository;
import com.skillsphere.skill.service.exception.DuplicateResourceException;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@Service
@Transactional
public class CompetencyFrameworkService {

    private final CompetencyFrameworkRepository competencyFrameworkRepository;
    private final FrameworkSkillRequirementRepository requirementRepository;
    private final EmployeeSkillMappingRepository employeeSkillMappingRepository;
    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final StudentRepository studentRepository;

    public CompetencyFrameworkService(
            CompetencyFrameworkRepository competencyFrameworkRepository,
            FrameworkSkillRequirementRepository requirementRepository,
            EmployeeSkillMappingRepository employeeSkillMappingRepository,
            EmployeeRepository employeeRepository,
            SkillRepository skillRepository,
            StudentRepository studentRepository) {
        this.competencyFrameworkRepository = competencyFrameworkRepository;
        this.requirementRepository = requirementRepository;
        this.employeeSkillMappingRepository = employeeSkillMappingRepository;
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
        this.studentRepository = studentRepository;
    }

    public CompetencyFramework createFramework(String name, String description, String roleTitle) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Framework name is required");
        }
        if (roleTitle == null || roleTitle.isBlank()) {
            throw new IllegalArgumentException("Role title is required");
        }
        CompetencyFramework framework = new CompetencyFramework(name, description, roleTitle);
        return competencyFrameworkRepository.save(framework);
    }

    public CompetencyFramework createFramework(String name, String description) {
        return createFramework(name, description, name);
    }

    public CompetencyFramework createFramework(CompetencyFramework framework) {
        if (framework == null || framework.getName() == null || framework.getName().isBlank()) {
            throw new IllegalArgumentException("Framework name is required");
        }
        if (framework.getRoleTitle() == null || framework.getRoleTitle().isBlank()) {
            throw new IllegalArgumentException("Role title is required");
        }
        return competencyFrameworkRepository.save(framework);
    }

    public CompetencyFramework getFrameworkById(Long id) {
        return competencyFrameworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Framework not found with id: " + id));
    }

    public List<CompetencyFramework> getAllFrameworks() {
        return competencyFrameworkRepository.findAll();
    }

    public FrameworkSkillRequirement addRequirement(Long frameworkId, Long skillId, Integer requiredProficiency, String notes) {
        if (frameworkId == null || frameworkId <= 0 || skillId == null || skillId <= 0) {
            throw new IllegalArgumentException("Framework and skill ids must be positive values");
        }
        CompetencyFramework framework = getFrameworkById(frameworkId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));

        if (requiredProficiency == null || requiredProficiency < 1 || requiredProficiency > 10) {
            throw new IllegalArgumentException("Required proficiency must be between 1 and 10");
        }

        if (requirementRepository.findByFramework_IdAndSkill_Id(frameworkId, skillId).isPresent()) {
            throw new DuplicateResourceException("This skill is already required by the framework");
        }

        FrameworkSkillRequirement requirement = new FrameworkSkillRequirement();
        requirement.setFramework(framework);
        requirement.setSkill(skill);
        requirement.setRequiredProficiency(requiredProficiency);
        requirement.setNotes(notes);
        return requirementRepository.save(requirement);
    }

    public List<FrameworkSkillRequirement> getRequirementsByFramework(Long frameworkId) {
        if (frameworkId == null || frameworkId <= 0) {
            throw new IllegalArgumentException("Framework id must be a positive value");
        }
        getFrameworkById(frameworkId);
        return requirementRepository.findByFramework_Id(frameworkId);
    }

    public FrameworkSkillRequirement updateRequirement(Long frameworkId, Long requirementId, Long skillId, Integer requiredProficiency, String notes) {
        CompetencyFramework framework = getFrameworkById(frameworkId);
        FrameworkSkillRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Framework requirement not found with id: " + requirementId));

        if (!framework.getId().equals(requirement.getFrameworkId())) {
            throw new IllegalArgumentException("Requirement does not belong to the selected framework");
        }

        if (skillId != null) {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
            requirement.setSkill(skill);
        }

        if (requiredProficiency != null) {
            if (requiredProficiency < 1 || requiredProficiency > 10) {
                throw new IllegalArgumentException("Required proficiency must be between 1 and 10");
            }
            requirement.setRequiredProficiency(requiredProficiency);
        }
        requirement.setNotes(notes);
        return requirementRepository.save(requirement);
    }

    public void deleteRequirement(Long frameworkId, Long requirementId) {
        getFrameworkById(frameworkId);
        FrameworkSkillRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Framework requirement not found with id: " + requirementId));
        if (!frameworkId.equals(requirement.getFrameworkId())) {
            throw new IllegalArgumentException("Requirement does not belong to the selected framework");
        }
        requirementRepository.delete(requirement);
    }

    public List<CompetencyGap> getGapReport(Long frameworkId, Long employeeId) {
        if (frameworkId == null || frameworkId <= 0 || employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("Framework and employee ids must be positive values");
        }
        getFrameworkById(frameworkId);
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        return calculateGapReport(frameworkId,
                employeeSkillMappingRepository.findByEmployee_Id(employeeId));
    }

    public List<CompetencyGap> getStudentGapReport(Long frameworkId, Long studentId) {
        if (frameworkId == null || frameworkId <= 0 || studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Framework and student ids must be positive values");
        }
        getFrameworkById(frameworkId);
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        return calculateGapReport(frameworkId,
                employeeSkillMappingRepository.findByStudent_Id(studentId));
    }

    public CompetencyGapReport getEmployeeGapSummary(Long frameworkId, Long employeeId) {
        return summarize(frameworkId, getGapReport(frameworkId, employeeId));
    }

    public CompetencyGapReport getStudentGapSummary(Long frameworkId, Long studentId) {
        return summarize(frameworkId, getStudentGapReport(frameworkId, studentId));
    }

    private List<CompetencyGap> calculateGapReport(Long frameworkId, List<EmployeeSkillMapping> mappings) {
        List<FrameworkSkillRequirement> requirements = requirementRepository.findByFramework_Id(frameworkId);
        List<CompetencyGap> gaps = new ArrayList<>();
        for (FrameworkSkillRequirement requirement : requirements) {
            int required = requirement.getRequiredProficiency();
            EmployeeSkillMapping mapping = mappings.stream()
                    .filter(candidate -> requirement.getSkillId().equals(candidate.getSkillId()))
                    .findFirst()
                    .orElse(null);
            Integer verifiedProficiency = mapping != null
                    && mapping.getVerificationStatus() == com.skillsphere.skill.entity.VerificationStatus.VERIFIED
                            ? mapping.getProficiency()
                            : null;
            int gap = Math.max(0, required - (verifiedProficiency == null ? 0 : verifiedProficiency));
            String status = mapping == null
                    ? "NOT_ASSIGNED"
                    : verifiedProficiency == null ? "UNVERIFIED" : gap == 0 ? "MATCHED" : "UNDER_REQUIRED";
            String evidenceStatus = mapping == null
                    ? "MISSING"
                    : mapping.getVerificationStatus().name();
            String recommendation = switch (status) {
                case "NOT_ASSIGNED" -> "Add this skill and request verification";
                case "UNVERIFIED" -> "Complete verification for this skill";
                case "UNDER_REQUIRED" -> "Improve verified proficiency by " + gap + " level(s)";
                default -> "Requirement satisfied";
            };
            gaps.add(new CompetencyGap(
                    requirement.getSkillId(),
                    requirement.getSkill() != null ? requirement.getSkill().getName() : null,
                    required,
                    verifiedProficiency,
                    gap,
                    status,
                    evidenceStatus,
                    recommendation));
        }
        return gaps;
    }

    private CompetencyGapReport summarize(Long frameworkId, List<CompetencyGap> gaps) {
        CompetencyFramework framework = getFrameworkById(frameworkId);
        int totalRequiredLevels = 0;
        int verifiedLevels = 0;
        int satisfied = 0;
        int missing = 0;
        int unverified = 0;
        int proficiencyGaps = 0;
        for (CompetencyGap gap : gaps) {
            int required = gap.getRequiredProficiency();
            int currentProficiency = Optional.ofNullable(gap.getEmployeeProficiency()).orElse(0);
            totalRequiredLevels += required;
            verifiedLevels += Math.min(required, currentProficiency);
            switch (gap.getStatus()) {
                case "MATCHED" -> satisfied++;
                case "NOT_ASSIGNED" -> missing++;
                case "UNVERIFIED" -> unverified++;
                case "UNDER_REQUIRED" -> proficiencyGaps++;
                default -> {
                }
            }
        }
        double readiness = totalRequiredLevels == 0 ? 100.0 : verifiedLevels * 100.0 / totalRequiredLevels;
        return new CompetencyGapReport(framework.getId(), framework.getName(), framework.getRoleTitle(),
                gaps.size(), satisfied, missing, unverified, proficiencyGaps, readiness,
                "Sum of each requirement's verified proficiency, capped at its required level, divided by the sum of required levels.",
                gaps);
    }

    public void deleteFramework(Long id) {
        CompetencyFramework framework = getFrameworkById(id);
        competencyFrameworkRepository.delete(framework);
    }
}
