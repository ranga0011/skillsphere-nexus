package com.skillsphere.skill.entity;

import java.util.List;

public record CompetencyGapReport(
        Long frameworkId,
        String frameworkName,
        String roleTitle,
        int requiredSkills,
        int satisfiedRequirements,
        int missingSkills,
        int unverifiedSkills,
        int proficiencyGaps,
        double readinessPercentage,
        String readinessFormula,
        List<CompetencyGap> skills) {
}
