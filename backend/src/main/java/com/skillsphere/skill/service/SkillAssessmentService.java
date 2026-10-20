package com.skillsphere.skill.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.Employee;
import com.skillsphere.skill.entity.Skill;
import com.skillsphere.skill.entity.SkillAssessment;
import com.skillsphere.skill.entity.VerificationStatus;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.repository.SkillAssessmentRepository;
import com.skillsphere.skill.repository.SkillRepository;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@Service
@Transactional
public class SkillAssessmentService {

    private final SkillAssessmentRepository skillAssessmentRepository;
    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;

    public SkillAssessmentService(
            SkillAssessmentRepository skillAssessmentRepository,
            EmployeeRepository employeeRepository,
            SkillRepository skillRepository) {
        this.skillAssessmentRepository = skillAssessmentRepository;
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
    }

    public SkillAssessment createAssessment(
            Long employeeId,
            Long skillId,
            Integer score,
            Integer maxScore,
            String assessmentMethod,
            String assessorName,
            LocalDate assessmentDate) {
        validateIds(employeeId, skillId);
        validateAssessment(score, maxScore, assessmentMethod, assessmentDate);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));

        SkillAssessment assessment = new SkillAssessment();
        assessment.setEmployee(employee);
        assessment.setSkill(skill);
        assessment.setScore(score);
        assessment.setMaxScore(maxScore);
        assessment.setAssessmentMethod(assessmentMethod);
        assessment.setAssessorName(assessorName);
        assessment.setAssessmentDate(assessmentDate);

        return skillAssessmentRepository.save(assessment);
    }

    public List<SkillAssessment> getAllAssessments() {
        return skillAssessmentRepository.findAll();
    }

    public SkillAssessment getAssessmentById(Long id) {
        return skillAssessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));
    }

    public List<SkillAssessment> getAssessmentsByEmployee(Long employeeId) {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("Employee id must be a positive value");
        }
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return skillAssessmentRepository.findByEmployee_Id(employeeId);
    }

    public List<SkillAssessment> getAssessmentsBySkill(Long skillId) {
        if (skillId == null || skillId <= 0) {
            throw new IllegalArgumentException("Skill id must be a positive value");
        }
        skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
        return skillAssessmentRepository.findBySkill_Id(skillId);
    }

    public List<SkillAssessment> filterAssessments(Long employeeId, Long skillId, VerificationStatus verificationStatus, LocalDate fromDate, LocalDate toDate) {
        List<SkillAssessment> all = new ArrayList<>(skillAssessmentRepository.findAll());

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        if (employeeId != null) {
            all.removeIf(assessment -> !employeeId.equals(assessment.getEmployeeId()));
        }
        if (skillId != null) {
            all.removeIf(assessment -> !skillId.equals(assessment.getSkillId()));
        }
        if (verificationStatus != null) {
            all.removeIf(assessment -> assessment.getVerificationStatus() != verificationStatus);
        }
        if (fromDate != null) {
            all.removeIf(assessment -> assessment.getAssessmentDate() == null || assessment.getAssessmentDate().isBefore(fromDate));
        }
        if (toDate != null) {
            all.removeIf(assessment -> assessment.getAssessmentDate() == null || assessment.getAssessmentDate().isAfter(toDate));
        }
        all.sort((left, right) -> right.getAssessmentDate().compareTo(left.getAssessmentDate()));
        return all;
    }

    public SkillAssessment updateAssessment(Long id, Long employeeId, Long skillId, Integer score, Integer maxScore,
            String assessmentMethod, String assessorName, LocalDate assessmentDate) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Assessment id must be a positive value");
        }
        SkillAssessment existing = getAssessmentById(id);

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

        if (score != null) {
            validateAssessment(score, maxScore == null ? existing.getMaxScore() : maxScore, assessmentMethod == null ? existing.getAssessmentMethod() : assessmentMethod, assessmentDate == null ? existing.getAssessmentDate() : assessmentDate);
            existing.setScore(score);
        }

        if (maxScore != null) {
            validateAssessment(existing.getScore(), maxScore, assessmentMethod == null ? existing.getAssessmentMethod() : assessmentMethod, assessmentDate == null ? existing.getAssessmentDate() : assessmentDate);
            existing.setMaxScore(maxScore);
        }

        if (assessmentMethod != null) {
            existing.setAssessmentMethod(assessmentMethod);
        }
        if (assessorName != null) {
            existing.setAssessorName(assessorName);
        }
        if (assessmentDate != null) {
            existing.setAssessmentDate(assessmentDate);
        }

        return skillAssessmentRepository.save(existing);
    }

    public SkillAssessment verifyAssessment(Long id, VerificationStatus verificationStatus) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Assessment id must be a positive value");
        }
        if (verificationStatus == null || verificationStatus == VerificationStatus.PENDING_VERIFICATION) {
            throw new IllegalArgumentException("Verification action must set VERIFIED or REJECTED");
        }

        SkillAssessment assessment = getAssessmentById(id);
        assessment.setVerificationStatus(verificationStatus);
        return skillAssessmentRepository.save(assessment);
    }

    public void deleteAssessment(Long id) {
        SkillAssessment assessment = getAssessmentById(id);
        skillAssessmentRepository.delete(assessment);
    }

    private void validateAssessment(Integer score, Integer maxScore, String assessmentMethod, LocalDate assessmentDate) {
        if (score == null) {
            throw new IllegalArgumentException("Score is required");
        }
        if (maxScore == null || maxScore <= 0) {
            throw new IllegalArgumentException("Maximum score must be greater than zero");
        }
        if (score < 0 || score > maxScore) {
            throw new IllegalArgumentException("Score must be between 0 and the maximum score");
        }
        if (assessmentMethod == null || assessmentMethod.isBlank()) {
            throw new IllegalArgumentException("Assessment method is required");
        }
        if (assessmentDate == null) {
            throw new IllegalArgumentException("Assessment date is required");
        }
        if (assessmentDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Assessment date cannot be in the future");
        }
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
