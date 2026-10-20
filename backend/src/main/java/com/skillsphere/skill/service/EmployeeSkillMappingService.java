package com.skillsphere.skill.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.Employee;
import com.skillsphere.skill.entity.EmployeeSkillMapping;
import com.skillsphere.skill.entity.Skill;
import com.skillsphere.skill.entity.VerificationStatus;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.repository.EmployeeSkillMappingRepository;
import com.skillsphere.skill.repository.SkillRepository;
import com.skillsphere.skill.service.exception.DuplicateResourceException;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@Service
@Transactional
public class EmployeeSkillMappingService {

    private final EmployeeSkillMappingRepository employeeSkillMappingRepository;
    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;

    public EmployeeSkillMappingService(
            EmployeeSkillMappingRepository employeeSkillMappingRepository,
            EmployeeRepository employeeRepository,
            SkillRepository skillRepository) {
        this.employeeSkillMappingRepository = employeeSkillMappingRepository;
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
    }

    public EmployeeSkillMapping assignSkill(Long employeeId, Long skillId, Integer proficiency, VerificationStatus verificationStatus, LocalDate assessmentDate) {
        validateIds(employeeId, skillId);
        if (proficiency == null || proficiency < 1 || proficiency > 10) {
            throw new IllegalArgumentException("Proficiency must be between 1 and 10");
        }
        if (assessmentDate != null && assessmentDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Assessment date cannot be in the future");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));

        if (employeeSkillMappingRepository.existsByEmployee_IdAndSkill_Id(employeeId, skillId)) {
            throw new DuplicateResourceException("Employee already has this skill assigned");
        }

        EmployeeSkillMapping mapping = new EmployeeSkillMapping();
        mapping.setEmployee(employee);
        mapping.setSkill(skill);
        mapping.setProficiency(proficiency);
        mapping.setVerificationStatus(verificationStatus);
        mapping.setAssessmentDate(assessmentDate);
        return employeeSkillMappingRepository.save(mapping);
    }

    public List<EmployeeSkillMapping> getAllMappings() {
        return employeeSkillMappingRepository.findAll();
    }

    public EmployeeSkillMapping getMappingById(Long id) {
        return employeeSkillMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee-skill mapping not found with id: " + id));
    }

    public List<EmployeeSkillMapping> getMappingsByEmployee(Long employeeId) {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("Employee id must be a positive value");
        }
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return employeeSkillMappingRepository.findByEmployee_Id(employeeId);
    }

    public List<EmployeeSkillMapping> getMappingsBySkill(Long skillId) {
        if (skillId == null || skillId <= 0) {
            throw new IllegalArgumentException("Skill id must be a positive value");
        }
        skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
        return employeeSkillMappingRepository.findBySkill_Id(skillId);
    }

    public EmployeeSkillMapping updateMapping(Long id, Long employeeId, Long skillId, Integer proficiency, VerificationStatus verificationStatus, LocalDate assessmentDate) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Mapping id must be a positive value");
        }
        EmployeeSkillMapping existing = getMappingById(id);

        Long targetEmployeeId = employeeId == null ? existing.getEmployeeId() : employeeId;
        Long targetSkillId = skillId == null ? existing.getSkillId() : skillId;
        if (employeeSkillMappingRepository.existsByEmployee_IdAndSkill_Id(targetEmployeeId, targetSkillId)
                && (!targetEmployeeId.equals(existing.getEmployeeId()) || !targetSkillId.equals(existing.getSkillId()))) {
            throw new DuplicateResourceException("Employee already has this skill assigned");
        }

        if (employeeId != null && !employeeId.equals(existing.getEmployeeId())) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
            existing.setEmployee(employee);
        }

        if (skillId != null && !skillId.equals(existing.getSkillId())) {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
            existing.setSkill(skill);
        }

        if (proficiency != null) {
            if (proficiency < 1 || proficiency > 10) {
                throw new IllegalArgumentException("Proficiency must be between 1 and 10");
            }
            existing.setProficiency(proficiency);
        }

        if (verificationStatus != null) {
            existing.setVerificationStatus(verificationStatus);
        }

        if (assessmentDate != null) {
            if (assessmentDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Assessment date cannot be in the future");
            }
            existing.setAssessmentDate(assessmentDate);
        }
        return employeeSkillMappingRepository.save(existing);
    }

    public void deleteMapping(Long id) {
        EmployeeSkillMapping mapping = getMappingById(id);
        employeeSkillMappingRepository.delete(mapping);
    }

    private void validateIds(Long employeeId, Long skillId) {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("Employee id must be a positive value");
        }
        if (skillId == null || skillId <= 0) {
            throw new IllegalArgumentException("Skill id must be a positive value");
        }
    }
}
