package com.skillsphere.skill.controller;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillsphere.skill.security.JwtService;
import com.skillsphere.skill.service.LearnerAssessmentService;
import com.skillsphere.skill.service.LearningModels.AssessmentView;
import com.skillsphere.skill.service.LearningModels.AttemptResult;
import com.skillsphere.skill.service.LearningModels.StartedAttempt;
import com.skillsphere.skill.service.LearningModels.SubmitAttemptRequest;

@RestController
@RequestMapping("/api/learning")
public class LearnerAssessmentController {
    private final LearnerAssessmentService assessmentService;

    public LearnerAssessmentController(LearnerAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping("/assessments")
    public List<AssessmentView> listAvailable(@AuthenticationPrincipal Jwt jwt) {
        Learner owner = owner(jwt);
        return assessmentService.listAvailable(owner.employeeId(), owner.studentId());
    }

    @PostMapping("/assessments/{id}/attempts")
    public StartedAttempt start(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Learner owner = owner(jwt);
        return assessmentService.start(id, owner.employeeId(), owner.studentId());
    }

    @PostMapping("/attempts/{id}/submit")
    public AttemptResult submit(@PathVariable Long id, @RequestBody SubmitAttemptRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Learner owner = owner(jwt);
        return assessmentService.submit(id, owner.employeeId(), owner.studentId(), request);
    }

    @GetMapping("/attempts")
    public List<AttemptResult> history(@AuthenticationPrincipal Jwt jwt) {
        Learner owner = owner(jwt);
        return assessmentService.history(owner.employeeId(), owner.studentId());
    }

    private Learner owner(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        if ("STUDENT".equals(role)) {
            Long studentId = JwtService.studentIdFromClaims(jwt.getClaims());
            if (studentId == null) {
                throw new AccessDeniedException("A linked student profile is required");
            }
            return new Learner(null, studentId);
        }
        if ("EMPLOYEE".equals(role)) {
            Long employeeId = JwtService.employeeIdFromClaims(jwt.getClaims());
            if (employeeId == null) {
                throw new AccessDeniedException("A linked employee profile is required");
            }
            return new Learner(employeeId, null);
        }
        throw new AccessDeniedException("Learner assessments are available to employees and students");
    }

    private record Learner(Long employeeId, Long studentId) {
    }
}
