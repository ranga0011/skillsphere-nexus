package com.skillsphere.skill.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.Employee;
import com.skillsphere.skill.entity.EmployeeSkillMapping;
import com.skillsphere.skill.entity.Skill;
import com.skillsphere.skill.entity.SkillAssessment;
import com.skillsphere.skill.entity.Student;
import com.skillsphere.skill.entity.VerificationStatus;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.repository.EmployeeSkillMappingRepository;
import com.skillsphere.skill.repository.SkillRepository;
import com.skillsphere.skill.repository.SkillAssessmentRepository;
import com.skillsphere.skill.repository.StudentRepository;
import com.skillsphere.skill.service.exception.DuplicateResourceException;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@Service
@Transactional
public class EmployeeSkillMappingService {

    private final EmployeeSkillMappingRepository employeeSkillMappingRepository;
    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final StudentRepository studentRepository;
    private final SkillAssessmentRepository skillAssessmentRepository;

    public EmployeeSkillMappingService(
            EmployeeSkillMappingRepository employeeSkillMappingRepository,
            EmployeeRepository employeeRepository,
            SkillRepository skillRepository,
            StudentRepository studentRepository,
            SkillAssessmentRepository skillAssessmentRepository) {
        this.employeeSkillMappingRepository = employeeSkillMappingRepository;
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
        this.studentRepository = studentRepository;
        this.skillAssessmentRepository = skillAssessmentRepository;
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

    public List<EmployeeSkillMapping> getMappingsByStudent(Long studentId) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Student id must be a positive value");
        }
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        return employeeSkillMappingRepository.findByStudent_Id(studentId);
    }

    public EmployeeSkillMapping requestSkillForStudent(Long studentId, Long skillId, Integer proficiency) {
        if (studentId == null || studentId <= 0 || skillId == null || skillId <= 0) {
            throw new IllegalArgumentException("Student and skill ids must be positive values");
        }
        if (proficiency == null || proficiency < 1 || proficiency > 10) {
            throw new IllegalArgumentException("Proficiency must be between 1 and 10");
        }
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
        if (employeeSkillMappingRepository.existsByStudent_IdAndSkill_Id(studentId, skillId)) {
            throw new DuplicateResourceException("Student already has this skill requested");
        }
        EmployeeSkillMapping mapping = new EmployeeSkillMapping();
        mapping.setStudent(student);
        mapping.setSkill(skill);
        mapping.setProficiency(proficiency);
        mapping.setVerificationStatus(VerificationStatus.PENDING_VERIFICATION);
        return employeeSkillMappingRepository.save(mapping);
    }

    public EmployeeSkillMapping recordVerifiedAssessment(SkillAssessment assessment) {
        if (assessment == null || assessment.getScore() == null
                || assessment.getMaxScore() == null || assessment.getMaxScore() <= 0
                || assessment.getSkill() == null) {
            throw new IllegalArgumentException("A complete assessment is required to record verified proficiency");
        }
        int level = Math.max(1, Math.min(10,
                (int) Math.ceil(assessment.getScore() * 10.0 / assessment.getMaxScore())));
        EmployeeSkillMapping mapping;
        if (assessment.getEmployee() != null) {
            mapping = employeeSkillMappingRepository
                    .findByEmployee_IdAndSkill_Id(assessment.getEmployee().getId(), assessment.getSkill().getId())
                    .orElseGet(EmployeeSkillMapping::new);
            mapping.setEmployee(assessment.getEmployee());
        } else if (assessment.getStudent() != null) {
            mapping = employeeSkillMappingRepository
                    .findByStudent_IdAndSkill_Id(assessment.getStudent().getId(), assessment.getSkill().getId())
                    .orElseGet(EmployeeSkillMapping::new);
            mapping.setStudent(assessment.getStudent());
        } else {
            throw new IllegalArgumentException("Assessment must be linked to a student or employee");
        }
        mapping.setSkill(assessment.getSkill());
        mapping.setProficiency(level);
        mapping.setVerificationStatus(VerificationStatus.VERIFIED);
        mapping.setAssessmentDate(assessment.getAssessmentDate());
        mapping.setVerifiedAssessment(assessment);
        return employeeSkillMappingRepository.save(mapping);
    }

    public void removeVerifiedAssessment(SkillAssessment assessment) {
        if (assessment == null || assessment.getSkill() == null) {
            return;
        }
        EmployeeSkillMapping mapping;
        if (assessment.getEmployee() != null) {
            mapping = employeeSkillMappingRepository
                    .findByEmployee_IdAndSkill_Id(assessment.getEmployee().getId(), assessment.getSkill().getId())
                    .orElse(null);
        } else if (assessment.getStudent() != null) {
            mapping = employeeSkillMappingRepository
                    .findByStudent_IdAndSkill_Id(assessment.getStudent().getId(), assessment.getSkill().getId())
                    .orElse(null);
        } else {
            return;
        }
        if (mapping == null || mapping.getVerifiedAssessment() == null
                || !assessment.getId().equals(mapping.getVerifiedAssessment().getId())) {
            return;
        }

        SkillAssessment replacement = assessment.getEmployee() != null
                ? skillAssessmentRepository
                        .findFirstByEmployee_IdAndSkill_IdAndVerificationStatusAndIdNotOrderByAssessmentDateDescIdDesc(
                                assessment.getEmployee().getId(), assessment.getSkill().getId(),
                                VerificationStatus.VERIFIED, assessment.getId())
                        .orElse(null)
                : skillAssessmentRepository
                        .findFirstByStudent_IdAndSkill_IdAndVerificationStatusAndIdNotOrderByAssessmentDateDescIdDesc(
                                assessment.getStudent().getId(), assessment.getSkill().getId(),
                                VerificationStatus.VERIFIED, assessment.getId())
                        .orElse(null);
        if (replacement != null) {
            recordVerifiedAssessment(replacement);
        } else {
            mapping.setVerifiedAssessment(null);
            mapping.setVerificationStatus(VerificationStatus.PENDING_VERIFICATION);
            employeeSkillMappingRepository.save(mapping);
        }
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
        if (employeeId != null || skillId != null || proficiency != null
                || verificationStatus != null || assessmentDate != null) {
            existing.setVerifiedAssessment(null);
        }

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
