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
            AssessmentDifficulty difficulty, Integer points, String explanation) {
        public QuestionRequest(String prompt, List<String> options, Integer correctOptionIndex,
                AssessmentDifficulty difficulty, Integer points) {
            this(prompt, options, correctOptionIndex, difficulty, points, null);
        }
    }

    public record AssessmentRequest(String title, String instructions, Long skillId,
            Integer passPercentage, Integer maxAttempts, Boolean active, Long courseId, String courseTitle) {
        public AssessmentRequest(String title, String instructions, Long skillId,
                Integer passPercentage, Integer maxAttempts, Boolean active) {
            this(title, instructions, skillId, passPercentage, maxAttempts, active, null, null);
        }
    }

    public record CreateAssessmentWithQuestionsRequest(
            String title,
            String instructions,
            Long skillId,
            Long courseId,
            String courseTitle,
            Integer passPercentage,
            Integer maxAttempts,
            Boolean active,
            List<QuestionRequest> questions
    ) {
    }

    public record AiGenerateQuestionsRequest(
            Long courseId,
            String courseTitle,
            String courseDescription,
            String courseCategory,
            Integer questionCount
    ) {
    }

    public record QuestionView(Long id, String prompt, List<String> options,
            AssessmentDifficulty difficulty, int points, String explanation) {
        public QuestionView(Long id, String prompt, List<String> options,
                AssessmentDifficulty difficulty, int points) {
            this(id, prompt, options, difficulty, points, null);
        }
    }

    public record AssessmentView(Long id, String title, String instructions, Long skillId,
            String skillName, int passPercentage, int maxAttempts, boolean active, int questionCount,
            Integer attemptsRemaining, Long courseId, String courseTitle) {
        public AssessmentView(Long id, String title, String instructions, Long skillId,
                String skillName, int passPercentage, int maxAttempts, boolean active, int questionCount,
                Integer attemptsRemaining) {
            this(id, title, instructions, skillId, skillName, passPercentage, maxAttempts, active,
                    questionCount, attemptsRemaining, null, null);
        }
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
            IssuedCertificateView certificate,
            boolean emailDispatched, String emailRecipient, String emailSubject, String emailBodyPreview,
            String emailStatus, String emailDeliveryMessage) {
        public AttemptResult(Long attemptId, Long assessmentId, String assessmentTitle,
                LearnerAttemptStatus status, Instant startedAt, Instant submittedAt, int attemptNumber,
                int scorePoints, int maxPoints, boolean passed, int passPercentage,
                IssuedCertificateView certificate,
                boolean emailDispatched, String emailRecipient, String emailSubject, String emailBodyPreview,
                String emailStatus, String emailDeliveryMessage) {
            this(attemptId, assessmentId, assessmentTitle, status, startedAt, submittedAt, attemptNumber,
                    scorePoints, maxPoints,
                    maxPoints > 0 ? (int) Math.floor(scorePoints * 100.0 / maxPoints) : 0,
                    passed, passPercentage, certificate,
                    emailDispatched, emailRecipient, emailSubject, emailBodyPreview, emailStatus, emailDeliveryMessage);
        }
        public AttemptResult(Long attemptId, Long assessmentId, String assessmentTitle,
                LearnerAttemptStatus status, Instant startedAt, Instant submittedAt, int attemptNumber,
                int scorePoints, int maxPoints, int percentage, boolean passed, int passPercentage,
                IssuedCertificateView certificate,
                boolean emailDispatched, String emailRecipient, String emailSubject, String emailBodyPreview) {
            this(attemptId, assessmentId, assessmentTitle, status, startedAt, submittedAt, attemptNumber,
                    scorePoints, maxPoints, percentage, passed, passPercentage, certificate,
                    emailDispatched, emailRecipient, emailSubject, emailBodyPreview, null, null);
        }
        public AttemptResult(Long attemptId, Long assessmentId, String assessmentTitle,
                LearnerAttemptStatus status, Instant startedAt, Instant submittedAt, int attemptNumber,
                int scorePoints, int maxPoints, int percentage, boolean passed, int passPercentage,
                IssuedCertificateView certificate) {
            this(attemptId, assessmentId, assessmentTitle, status, startedAt, submittedAt, attemptNumber,
                    scorePoints, maxPoints, percentage, passed, passPercentage, certificate,
                    false, null, null, null, null, null);
        }
    }

    public record ScorecardEmailView(
            Long id,
            Long attemptId,
            Long assessmentId,
            String assessmentTitle,
            Long studentId,
            Long employeeId,
            String recipientEmail,
            String recipientName,
            int scorePoints,
            int maxPoints,
            int percentage,
            boolean passed,
            String subject,
            String bodyText,
            String status,
            Instant sentAt
    ) {
    }

    public record QuestionReviewItem(
            Long id,
            String prompt,
            List<String> options,
            Integer correctOptionIndex,
            Integer selectedOptionIndex,
            boolean isCorrect,
            AssessmentDifficulty difficulty,
            int points,
            String explanation
    ) {
    }

    public record AssessmentReviewView(
            Long assessmentId,
            String assessmentTitle,
            String courseTitle,
            Long attemptId,
            Integer attemptNumber,
            Integer totalAttempts,
            Integer scorePoints,
            Integer maxPoints,
            Integer percentage,
            boolean passed,
            Integer passPercentage,
            List<QuestionReviewItem> questions
    ) {
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
