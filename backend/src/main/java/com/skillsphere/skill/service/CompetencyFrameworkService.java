package com.skillsphere.skill.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.CompetencyFramework;
import com.skillsphere.skill.entity.CompetencyGap;
import com.skillsphere.skill.entity.EmployeeSkillMapping;
import com.skillsphere.skill.entity.FrameworkSkillRequirement;
import com.skillsphere.skill.entity.Skill;
import com.skillsphere.skill.repository.CompetencyFrameworkRepository;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.repository.EmployeeSkillMappingRepository;
import com.skillsphere.skill.repository.FrameworkSkillRequirementRepository;
import com.skillsphere.skill.repository.SkillRepository;
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

    public CompetencyFrameworkService(
            CompetencyFrameworkRepository competencyFrameworkRepository,
            FrameworkSkillRequirementRepository requirementRepository,
            EmployeeSkillMappingRepository employeeSkillMappingRepository,
            EmployeeRepository employeeRepository,
            SkillRepository skillRepository) {
        this.competencyFrameworkRepository = competencyFrameworkRepository;
        this.requirementRepository = requirementRepository;
        this.employeeSkillMappingRepository = employeeSkillMappingRepository;
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
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

        List<FrameworkSkillRequirement> requirements = requirementRepository.findByFramework_Id(frameworkId);
        List<EmployeeSkillMapping> mappings = employeeSkillMappingRepository.findByEmployee_Id(employeeId);
        List<CompetencyGap> gaps = new ArrayList<>();

        for (FrameworkSkillRequirement requirement : requirements) {
            Integer requiredProficiency = requirement.getRequiredProficiency();
            Integer employeeProficiency = null;
            for (EmployeeSkillMapping mapping : mappings) {
                if (requirement.getSkillId().equals(mapping.getSkillId())) {
                    employeeProficiency = mapping.getProficiency();
                    break;
                }
            }

            String status;
            int gap;
            if (employeeProficiency == null) {
                gap = requiredProficiency;
                status = "NOT_ASSIGNED";
            } else if (employeeProficiency >= requiredProficiency) {
                gap = 0;
                status = "MATCHED";
            } else {
                gap = requiredProficiency - employeeProficiency;
                status = "UNDER_REQUIRED";
            }

            gaps.add(new CompetencyGap(
                    requirement.getSkillId(),
                    requirement.getSkill() != null ? requirement.getSkill().getName() : null,
                    requiredProficiency,
                    employeeProficiency,
                    gap,
                    status));
        }

        return gaps;
    }

    public void deleteFramework(Long id) {
        CompetencyFramework framework = getFrameworkById(id);
        competencyFrameworkRepository.delete(framework);
    }
}
