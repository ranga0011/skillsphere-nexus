package com.skillsphere.skill.service;

import java.time.Instant;
import java.util.List;

import com.skillsphere.skill.entity.AssessmentDifficulty;
import com.skillsphere.skill.entity.LearnerAttemptStatus;
import com.skillsphere.skill.service.CertificateModels.IssuedCertificateView;

public final class LearningModels {
    private LearningModels() {
    }

    public record QuestionRequest(String prompt, List<String> options, Integer correctOptionIndex,
            AssessmentDifficulty difficulty, Integer points) {
    }

    public record AssessmentRequest(String title, String instructions, Long skillId,
            Integer passPercentage, Integer maxAttempts, Boolean active) {
    }

    public record QuestionView(Long id, String prompt, List<String> options,
            AssessmentDifficulty difficulty, int points) {
    }

    public record AssessmentView(Long id, String title, String instructions, Long skillId,
            String skillName, int passPercentage, int maxAttempts, boolean active, int questionCount,
            Integer attemptsRemaining) {
    }

    public record StartedAttempt(Long attemptId, int attemptNumber, Instant startedAt,
            AssessmentView assessment, List<QuestionView> questions) {
    }

    public record SubmittedAnswer(Long questionId, Integer selectedOptionIndex) {
    }

    public record SubmitAttemptRequest(List<SubmittedAnswer> answers) {
    }

    public record AttemptResult(Long attemptId, Long assessmentId, String assessmentTitle,
            LearnerAttemptStatus status, Instant startedAt, Instant submittedAt, int attemptNumber,
            int scorePoints, int maxPoints, int percentage, boolean passed, int passPercentage,
            IssuedCertificateView certificate) {
    }

    public record UserAccountView(Long id, String username, String role, Long employeeId,
            Long studentId, boolean enabled) {
    }

    public record AccountStatusRequest(boolean enabled) {
    }

    public record AccountStatusChanged(UserAccountView account, Instant changedAt) {
    }

    public record LearningResourceView(Long id, String title, String description, Long skillId,
            String skillName, String resourceType, String difficultyLevel, String verifiedUrl, boolean active) {
    }

    public record LearningResourceRequest(String title, String description, Long skillId,
            String resourceType, String difficultyLevel, String verifiedUrl, Boolean active) {
    }

    public record ResourceRecommendation(LearningResourceView resource, int priorityGap,
            int currentVerifiedProficiency, int requiredProficiency) {
    }

    public record GapSkillAnalytics(Long skillId, String skillName, int requiredProficiency,
            int learnersWithGap, double learnersWithGapPercentage, int totalGapLevels,
            int awaitingAssessment, int awaitingVerification) {
    }

    public record CertificateExpirySummary(long activeCertificates, long expiringWithin30Days,
            long expiredCertificates) {
    }

    public record OrganizationGapAnalytics(Long frameworkId, String frameworkName, int totalLearners,
            int learnersMeetingAllRequirements, double learnersMeetingPercentage, int requiredSkills,
            CertificateExpirySummary certificateExpiry, List<GapSkillAnalytics> skills,
            long totalSkillEntries, int page, int size) {
    }
}
