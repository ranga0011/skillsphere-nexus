package com.skillsphere.skill.controller;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillsphere.skill.security.JwtService;
import com.skillsphere.skill.service.LearningModels.ResourceRecommendation;
import com.skillsphere.skill.service.QuestionBankService;

@RestController
@RequestMapping("/api/learning/resources")
public class LearningResourceController {
    private final QuestionBankService questionBankService;

    public LearningResourceController(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    @GetMapping("/recommendations")
    public List<ResourceRecommendation> getRecommendations(@RequestParam Long frameworkId,
            @AuthenticationPrincipal Jwt jwt) {
        if (!"STUDENT".equals(jwt.getClaimAsString("role"))) {
            throw new AccessDeniedException("Student resource recommendations are restricted to students");
        }
        Long studentId = JwtService.studentIdFromClaims(jwt.getClaims());
        if (studentId == null) {
            throw new AccessDeniedException("A linked student profile is required");
        }
        return questionBankService.getRecommendations(frameworkId, studentId);
    }
}
