package com.skillsphere.skill.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.skillsphere.skill.entity.Student;
import com.skillsphere.skill.entity.EmployeeSkillMapping;
import com.skillsphere.skill.entity.SkillAssessment;
import com.skillsphere.skill.security.AuthorizationService;
import com.skillsphere.skill.security.JwtService;
import com.skillsphere.skill.service.EmployeeSkillMappingService;
import com.skillsphere.skill.service.SkillAssessmentService;
import com.skillsphere.skill.service.StudentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final AuthorizationService authorizationService;
    private final EmployeeSkillMappingService mappingService;
    private final SkillAssessmentService assessmentService;

    public StudentController(StudentService studentService, AuthorizationService authorizationService,
            EmployeeSkillMappingService mappingService, SkillAssessmentService assessmentService) {
        this.studentService = studentService;
        this.authorizationService = authorizationService;
        this.mappingService = mappingService;
        this.assessmentService = assessmentService;
    }

    @GetMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/me")
    public Student getCurrentStudent(@AuthenticationPrincipal Jwt jwt) {
        Long studentId = JwtService.studentIdFromClaims(jwt.getClaims());
        authorizationService.requireStudentAccess(studentId);
        return studentService.getStudentById(studentId);
    }

    @PutMapping("/me")
    public Student updateCurrentStudent(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateStudentRequest request) {
        Long studentId = JwtService.studentIdFromClaims(jwt.getClaims());
        authorizationService.requireStudentAccess(studentId);
        return studentService.updateStudent(studentId, request.name(), request.email(), request.program());
    }

    @GetMapping("/me/skills")
    public List<EmployeeSkillMapping> getCurrentStudentSkills(@AuthenticationPrincipal Jwt jwt) {
        Long studentId = JwtService.studentIdFromClaims(jwt.getClaims());
        authorizationService.requireStudentAccess(studentId);
        return mappingService.getMappingsByStudent(studentId);
    }

    @org.springframework.web.bind.annotation.PostMapping("/me/skills")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public EmployeeSkillMapping requestSkill(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody StudentSkillRequest request) {
        Long studentId = JwtService.studentIdFromClaims(jwt.getClaims());
        authorizationService.requireStudentAccess(studentId);
        return mappingService.requestSkillForStudent(studentId, request.skillId(), request.proficiency());
    }

    @GetMapping("/me/assessments")
    public List<SkillAssessment> getCurrentStudentAssessments(@AuthenticationPrincipal Jwt jwt) {
        Long studentId = JwtService.studentIdFromClaims(jwt.getClaims());
        authorizationService.requireStudentAccess(studentId);
        return assessmentService.getAssessmentsByStudent(studentId);
    }

    @GetMapping("/{id}/skills")
    public List<EmployeeSkillMapping> getStudentSkills(@PathVariable Long id) {
        authorizationService.requireStudentAccess(id);
        return mappingService.getMappingsByStudent(id);
    }

    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Long id) {
        authorizationService.requireStudentAccess(id);
        return studentService.getStudentById(id);
    }

    public record UpdateStudentRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(max = 150) String program) {
    }

    public record StudentSkillRequest(
            @NotNull Long skillId,
            @NotNull @Min(1) @Max(10) Integer proficiency) {
    }
}
