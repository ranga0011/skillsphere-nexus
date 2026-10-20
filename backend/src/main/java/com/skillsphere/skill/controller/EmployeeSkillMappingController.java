package com.skillsphere.skill.controller;

import java.time.LocalDate;
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

import com.skillsphere.skill.entity.EmployeeSkillMapping;
import com.skillsphere.skill.service.EmployeeSkillMappingService;
import com.skillsphere.skill.security.AuthorizationService;

@RestController
@RequestMapping("/api/employee-skills")
public class EmployeeSkillMappingController {

    private final EmployeeSkillMappingService employeeSkillMappingService;
    private final AuthorizationService authorizationService;

    public EmployeeSkillMappingController(EmployeeSkillMappingService employeeSkillMappingService,
            AuthorizationService authorizationService) {
        this.employeeSkillMappingService = employeeSkillMappingService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    public List<EmployeeSkillMapping> getAllMappings() {
        return employeeSkillMappingService.getAllMappings();
    }

    @GetMapping("/{id}")
    public EmployeeSkillMapping getMappingById(@PathVariable Long id) {
        EmployeeSkillMapping mapping = employeeSkillMappingService.getMappingById(id);
        authorizationService.requireEmployeeAccess(mapping.getEmployeeId());
        return mapping;
    }

    @GetMapping("/employee/{employeeId}")
    public List<EmployeeSkillMapping> getMappingsByEmployee(@PathVariable Long employeeId) {
        authorizationService.requireEmployeeAccess(employeeId);
        return employeeSkillMappingService.getMappingsByEmployee(employeeId);
    }

    @GetMapping("/skill/{skillId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public List<EmployeeSkillMapping> getMappingsBySkill(@PathVariable Long skillId) {
        return employeeSkillMappingService.getMappingsBySkill(skillId);
    }

    @PostMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeSkillMapping assignSkill(@RequestBody EmployeeSkillMapping request) {
        return employeeSkillMappingService.assignSkill(
                request.getEmployeeId(),
                request.getSkillId(),
                request.getProficiency(),
                request.getVerificationStatus(),
                request.getAssessmentDate() == null ? LocalDate.now() : request.getAssessmentDate());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public EmployeeSkillMapping updateMapping(@PathVariable Long id, @RequestBody EmployeeSkillMapping request) {
        return employeeSkillMappingService.updateMapping(
                id,
                request.getEmployeeId(),
                request.getSkillId(),
                request.getProficiency(),
                request.getVerificationStatus(),
                request.getAssessmentDate());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMapping(@PathVariable Long id) {
        employeeSkillMappingService.deleteMapping(id);
    }
}
