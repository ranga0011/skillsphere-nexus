package com.skillsphere.skill.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.skillsphere.skill.entity.SkillAssessment;
import com.skillsphere.skill.entity.VerificationStatus;
import com.skillsphere.skill.service.SkillAssessmentService;
import com.skillsphere.skill.security.AuthorizationService;

@RestController
@RequestMapping("/api/assessments")
public class SkillAssessmentController {

    private final SkillAssessmentService skillAssessmentService;
    private final AuthorizationService authorizationService;

    public SkillAssessmentController(SkillAssessmentService skillAssessmentService,
            AuthorizationService authorizationService) {
        this.skillAssessmentService = skillAssessmentService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<SkillAssessment> getAllAssessments(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long skillId,
            @RequestParam(required = false) VerificationStatus verificationStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        if (employeeId == null) {
            if (!authorizationService.isHrAdmin()) {
                throw new org.springframework.security.access.AccessDeniedException("Employee filter is required");
            }
        } else {
            authorizationService.requireEmployeeAccess(employeeId);
        }
        return skillAssessmentService.filterAssessments(employeeId, skillId, verificationStatus, fromDate, toDate);
    }

    @GetMapping("/{id}")
    public SkillAssessment getAssessmentById(@PathVariable Long id) {
        SkillAssessment assessment = skillAssessmentService.getAssessmentById(id);
        authorizationService.requireProfileAccess(assessment.getEmployeeId(), assessment.getStudentId());
        return assessment;
    }

    @GetMapping("/employee/{employeeId}")
    public List<SkillAssessment> getAssessmentsByEmployee(@PathVariable Long employeeId) {
        authorizationService.requireEmployeeAccess(employeeId);
        return skillAssessmentService.getAssessmentsByEmployee(employeeId);
    }

    @GetMapping("/student/{studentId}")
    public List<SkillAssessment> getAssessmentsByStudent(@PathVariable Long studentId) {
        authorizationService.requireStudentAccess(studentId);
        return skillAssessmentService.getAssessmentsByStudent(studentId);
    }

    @GetMapping("/skill/{skillId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public List<SkillAssessment> getAssessmentsBySkill(@PathVariable Long skillId) {
        return skillAssessmentService.getAssessmentsBySkill(skillId);
    }

    @PostMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillAssessment createAssessment(@RequestBody SkillAssessment request) {
        if (request.getStudentId() != null) {
            return skillAssessmentService.createStudentAssessment(
                    request.getStudentId(),
                    request.getSkillId(),
                    request.getScore(),
                    request.getMaxScore(),
                    request.getAssessmentMethod(),
                    request.getAssessorName(),
                    request.getAssessmentDate());
        }
        return skillAssessmentService.createAssessment(
                request.getEmployeeId(),
                request.getSkillId(),
                request.getScore(),
                request.getMaxScore(),
                request.getAssessmentMethod(),
                request.getAssessorName(),
                request.getAssessmentDate());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public SkillAssessment updateAssessment(@PathVariable Long id, @RequestBody SkillAssessment request) {
        return skillAssessmentService.updateAssessment(
                id,
                request.getEmployeeId(),
                request.getSkillId(),
                request.getScore(),
                request.getMaxScore(),
                request.getAssessmentMethod(),
                request.getAssessorName(),
                request.getAssessmentDate());
    }

    @PutMapping("/{id}/verification")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public SkillAssessment verifyAssessment(@PathVariable Long id,
            @RequestBody Map<String, VerificationStatus> request,
            Authentication authentication) {
        return skillAssessmentService.verifyAssessment(
                id, request.get("verificationStatus"), authentication.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAssessment(@PathVariable Long id) {
        skillAssessmentService.deleteAssessment(id);
    }
}
