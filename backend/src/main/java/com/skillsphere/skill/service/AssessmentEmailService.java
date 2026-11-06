package com.skillsphere.skill.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.AssessmentScorecardEmail;
import com.skillsphere.skill.entity.LearnerAssessmentAttempt;
import com.skillsphere.skill.repository.AssessmentScorecardEmailRepository;
import com.skillsphere.skill.service.CertificateModels.IssuedCertificateView;
import com.skillsphere.skill.service.LearningModels.ScorecardEmailView;

@Service
@Transactional
public class AssessmentEmailService {
    private static final Logger logger = LoggerFactory.getLogger(AssessmentEmailService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final AssessmentScorecardEmailRepository emailRepository;

    public AssessmentEmailService(AssessmentScorecardEmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    public AssessmentScorecardEmail sendScorecardEmail(LearnerAssessmentAttempt attempt,
            int earnedPoints, int maximumPoints, int percentage, boolean passed,
            IssuedCertificateView certificate) {

        String recipientEmail = "learner@skillsphere.com";
        String recipientName = "SkillSphere Learner";
        Long studentId = null;
        Long employeeId = null;

        if (attempt.getStudent() != null) {
            recipientEmail = attempt.getStudent().getEmail();
            recipientName = attempt.getStudent().getName();
            studentId = attempt.getStudent().getId();
        } else if (attempt.getEmployee() != null) {
            recipientEmail = attempt.getEmployee().getEmail();
            recipientName = attempt.getEmployee().getName();
            employeeId = attempt.getEmployee().getId();
        }

        String assessmentTitle = attempt.getAssessment().getTitle();
        String trackName = attempt.getAssessment().getCourseTitle() != null
                && !attempt.getAssessment().getCourseTitle().isBlank()
                ? attempt.getAssessment().getCourseTitle()
                : attempt.getAssessment().getSkill().getName();

        String statusBadge = passed ? "CLEARED / PASSED" : "NOT PASSED (RETRY AVAILABLE)";
        String subject = String.format("[SkillSphere Nexus] Assessment Scorecard: %s - %d/%d Marks (%d%%) - %s",
                assessmentTitle, earnedPoints, maximumPoints, percentage, passed ? "PASSED" : "RESULT");

        StringBuilder body = new StringBuilder();
        body.append("======================================================================\n");
        body.append("           SKILLSPHERE NEXUS - OFFICIAL ASSESSMENT SCORECARD          \n");
        body.append("======================================================================\n\n");
        body.append("Dear ").append(recipientName).append(",\n\n");
        body.append("Your assessment submission has been evaluated and officially scored.\n\n");
        body.append("----------------------------------------------------------------------\n");
        body.append("  EXAMINATION PERFORMANCE BREAKDOWN\n");
        body.append("----------------------------------------------------------------------\n");
        body.append("  Assessment Title    : ").append(assessmentTitle).append("\n");
        body.append("  Course / Subject    : ").append(trackName).append("\n");
        body.append("  Attempt Number      : ").append(attempt.getAttemptNumber())
                .append(" of ").append(attempt.getAssessment().getMaxAttempts()).append("\n");
        body.append("  Marks Obtained      : ").append(earnedPoints).append(" / ").append(maximumPoints).append(" Marks\n");
        body.append("  Final Percentage    : ").append(percentage).append("%\n");
        body.append("  Passing Threshold   : ").append(attempt.getAssessment().getPassPercentage()).append("%\n");
        body.append("  Outcome Status      : ").append(statusBadge).append("\n");
        body.append("  Evaluation Time     : ").append(FORMATTER.format(Instant.now())).append("\n");

        if (certificate != null) {
            body.append("  Issued Certificate  : ").append(certificate.certificateNumber()).append("\n");
            body.append("  Verification Token  : ").append(certificate.verificationToken()).append("\n");
        }
        body.append("----------------------------------------------------------------------\n\n");

        if (passed) {
            body.append("CONGRATULATIONS!\n");
            body.append("You have successfully cleared this course assignment with a verified passing grade.\n");
            if (certificate != null) {
                body.append("Your accredited digital achievement certificate has been generated and is ready to download in your portal.\n");
            }
        } else {
            body.append("NOTICE:\n");
            body.append("You did not reach the required passing threshold of ")
                    .append(attempt.getAssessment().getPassPercentage()).append("% on this attempt.\n");
            int remaining = attempt.getAssessment().getMaxAttempts() - attempt.getAttemptNumber();
            if (remaining > 0) {
                body.append("You have ").append(remaining).append(" remaining attempt(s). We encourage you to review the course material and re-attempt.\n");
            } else {
                body.append("You have used all attempts allocated for this assignment. Please reach out to your instructor for guidance.\n");
            }
        }

        body.append("\nBest regards,\n");
        body.append("SkillSphere Nexus Automated Assessment Engine\n");
        body.append("Talent & Competency Management Platform\n");
        body.append("======================================================================\n");

        String bodyText = body.toString();

        AssessmentScorecardEmail scorecardEmail = new AssessmentScorecardEmail(
                attempt.getId(),
                attempt.getAssessment().getId(),
                assessmentTitle,
                studentId,
                employeeId,
                recipientEmail,
                recipientName,
                earnedPoints,
                maximumPoints,
                percentage,
                passed,
                subject,
                bodyText
        );

        AssessmentScorecardEmail saved = emailRepository.save(scorecardEmail);

        logger.info("\n>>> [SCORECARD EMAIL DISPATCHED] >>>\nTo: {}\nSubject: {}\nMarks: {}/{} ({}%)\nPassed: {}\nEmail Log ID: {}\n<<<",
                recipientEmail, subject, earnedPoints, maximumPoints, percentage, passed, saved.getId());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<ScorecardEmailView> getEmailsForStudent(Long studentId) {
        return emailRepository.findByStudentIdOrderBySentAtDesc(studentId).stream()
                .map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public List<ScorecardEmailView> getEmailsForEmployee(Long employeeId) {
        return emailRepository.findByEmployeeIdOrderBySentAtDesc(employeeId).stream()
                .map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public List<ScorecardEmailView> getEmailsByRecipient(String email) {
        return emailRepository.findByRecipientEmailIgnoreCaseOrderBySentAtDesc(email).stream()
                .map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public List<ScorecardEmailView> getAllEmails() {
        return emailRepository.findAllByOrderBySentAtDesc().stream()
                .map(this::toView).toList();
    }

    private ScorecardEmailView toView(AssessmentScorecardEmail email) {
        return new ScorecardEmailView(
                email.getId(),
                email.getAttemptId(),
                email.getAssessmentId(),
                email.getAssessmentTitle(),
                email.getStudentId(),
                email.getEmployeeId(),
                email.getRecipientEmail(),
                email.getRecipientName(),
                email.getScorePoints(),
                email.getMaxPoints(),
                email.getPercentage(),
                email.isPassed(),
                email.getSubject(),
                email.getBodyText(),
                email.getStatus(),
                email.getSentAt()
        );
    }
}
