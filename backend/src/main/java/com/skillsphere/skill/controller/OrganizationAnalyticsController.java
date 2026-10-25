package com.skillsphere.skill.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillsphere.skill.service.LearningModels.OrganizationGapAnalytics;
import com.skillsphere.skill.service.OrganizationAnalyticsService;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('HR_ADMIN')")
public class OrganizationAnalyticsController {
    private final OrganizationAnalyticsService analyticsService;

    public OrganizationAnalyticsController(OrganizationAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/skill-gaps")
    public OrganizationGapAnalytics getSkillGapAnalytics(
            @RequestParam Long frameworkId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String skillFilter) {
        return analyticsService.getFrameworkAnalytics(frameworkId, page, size, skillFilter);
    }
}
