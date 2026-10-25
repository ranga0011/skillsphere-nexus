package com.skillsphere.skill.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.skillsphere.skill.entity.CompetencyFramework;
import com.skillsphere.skill.entity.CompetencyGap;
import com.skillsphere.skill.entity.CompetencyGapReport;
import com.skillsphere.skill.entity.FrameworkSkillRequirement;
import com.skillsphere.skill.service.CompetencyFrameworkService;
import com.skillsphere.skill.security.AuthorizationService;
import com.skillsphere.skill.security.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/frameworks")
public class CompetencyFrameworkController {

    private final CompetencyFrameworkService competencyFrameworkService;
    private final AuthorizationService authorizationService;

    public CompetencyFrameworkController(CompetencyFrameworkService competencyFrameworkService,
            AuthorizationService authorizationService) {
        this.competencyFrameworkService = competencyFrameworkService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<CompetencyFramework> getAllFrameworks() {
        return competencyFrameworkService.getAllFrameworks();
    }

    @GetMapping("/{id}")
    public CompetencyFramework getFrameworkById(@PathVariable Long id) {
        return competencyFrameworkService.getFrameworkById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CompetencyFramework createFramework(@Valid @RequestBody CompetencyFramework framework) {
        return competencyFrameworkService.createFramework(framework);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public CompetencyFramework updateFramework(@PathVariable Long id, @Valid @RequestBody CompetencyFramework framework) {
        CompetencyFramework existing = competencyFrameworkService.getFrameworkById(id);
        existing.setName(framework.getName());
        existing.setDescription(framework.getDescription());
        existing.setRoleTitle(framework.getRoleTitle());
        return competencyFrameworkService.createFramework(existing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFramework(@PathVariable Long id) {
        competencyFrameworkService.deleteFramework(id);
    }

    @GetMapping("/{frameworkId}/requirements")
    public List<FrameworkSkillRequirement> getRequirements(@PathVariable Long frameworkId) {
        return competencyFrameworkService.getRequirementsByFramework(frameworkId);
    }

    @PostMapping("/{frameworkId}/requirements")
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public FrameworkSkillRequirement addRequirement(@PathVariable Long frameworkId, @RequestBody FrameworkSkillRequirement requirement) {
        return competencyFrameworkService.addRequirement(
                frameworkId,
                requirement.getSkillId(),
                requirement.getRequiredProficiency(),
                requirement.getNotes());
    }

    @PutMapping("/{frameworkId}/requirements/{requirementId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public FrameworkSkillRequirement updateRequirement(@PathVariable Long frameworkId,
            @PathVariable Long requirementId,
            @RequestBody FrameworkSkillRequirement requirement) {
        return competencyFrameworkService.updateRequirement(
                frameworkId,
                requirementId,
                requirement.getSkillId(),
                requirement.getRequiredProficiency(),
                requirement.getNotes());
    }

    @DeleteMapping("/{frameworkId}/requirements/{requirementId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRequirement(@PathVariable Long frameworkId, @PathVariable Long requirementId) {
        competencyFrameworkService.deleteRequirement(frameworkId, requirementId);
    }

    @GetMapping("/{frameworkId}/gap-report/{employeeId}")
    public List<CompetencyGap> getGapReport(@PathVariable Long frameworkId, @PathVariable Long employeeId) {
        authorizationService.requireEmployeeAccess(employeeId);
        return competencyFrameworkService.getGapReport(frameworkId, employeeId);
    }

    @GetMapping("/{frameworkId}/gap-report/student/{studentId}")
    public List<CompetencyGap> getStudentGapReport(
            @PathVariable Long frameworkId, @PathVariable Long studentId) {
        authorizationService.requireStudentAccess(studentId);
        return competencyFrameworkService.getStudentGapReport(frameworkId, studentId);
    }

    @GetMapping("/{frameworkId}/gap-report/me")
    public CompetencyGapReport getCurrentUserGapReport(
            @PathVariable Long frameworkId, @AuthenticationPrincipal Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        if ("STUDENT".equals(role)) {
            Long studentId = JwtService.studentIdFromClaims(jwt.getClaims());
            authorizationService.requireStudentAccess(studentId);
            return competencyFrameworkService.getStudentGapSummary(frameworkId, studentId);
        }
        if ("EMPLOYEE".equals(role)) {
            Long employeeId = JwtService.employeeIdFromClaims(jwt.getClaims());
            authorizationService.requireEmployeeAccess(employeeId);
            return competencyFrameworkService.getEmployeeGapSummary(frameworkId, employeeId);
        }
        throw new org.springframework.security.access.AccessDeniedException(
                "A linked employee or student profile is required");
    }
}
