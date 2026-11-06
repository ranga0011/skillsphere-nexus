package com.skillsphere.skill.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String mailUsername;
    private final String mailPassword;
    private final String senderName;

    public AssessmentEmailService(
            AssessmentScorecardEmailRepository emailRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword,
            @Value("${spring.mail.sender.name:SkillSphere Nexus Assessment Engine}") String senderName) {
        this.emailRepository = emailRepository;
        this.mailSenderProvider = mailSenderProvider;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
        this.senderName = senderName;
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
        String htmlBody = buildHtmlScorecard(recipientName, assessmentTitle, trackName,
                attempt.getAttemptNumber(), attempt.getAssessment().getMaxAttempts(),
                earnedPoints, maximumPoints, percentage, attempt.getAssessment().getPassPercentage(),
                passed, certificate);

        boolean smtpConfigured = mailUsername != null && !mailUsername.isBlank()
                && mailPassword != null && !mailPassword.isBlank();
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        String status = "PENDING_SMTP_CONFIG";
        if (smtpConfigured && mailSender != null) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(new InternetAddress(mailUsername, senderName));
                helper.setTo(recipientEmail);
                helper.setSubject(subject);
                helper.setText(bodyText, htmlBody);
                mailSender.send(mimeMessage);
                status = "DELIVERED";
                logger.info("Real scorecard email successfully transmitted to {} via Gmail SMTP", recipientEmail);
            } catch (Exception ex) {
                status = "FAILED_SMTP: " + ex.getMessage();
                logger.error("Failed to transmit email via SMTP to {}: {}", recipientEmail, ex.getMessage(), ex);
            }
        } else {
            status = "PENDING_SMTP_CONFIG";
            logger.warn("Live email to {} not transmitted: SMTP credentials (spring.mail.username / spring.mail.password) are not yet configured in application-local.properties.", recipientEmail);
        }

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
        scorecardEmail.setStatus(status);

        AssessmentScorecardEmail saved = emailRepository.save(scorecardEmail);

        logger.info("\n>>> [SCORECARD EMAIL RECORDED] >>>\nTo: {}\nSubject: {}\nMarks: {}/{} ({}%)\nPassed: {}\nStatus: {}\nEmail Log ID: {}\n<<<",
                recipientEmail, subject, earnedPoints, maximumPoints, percentage, passed, status, saved.getId());

        return saved;
    }

    public TestEmailResult testSmtpConnection(String testRecipient) {
        boolean smtpConfigured = mailUsername != null && !mailUsername.isBlank()
                && mailPassword != null && !mailPassword.isBlank();
        if (!smtpConfigured) {
            return new TestEmailResult(false,
                    "Gmail SMTP credentials are not configured. To send live emails to " + testRecipient + ", please add 'spring.mail.username' and 'spring.mail.password' (16-char Google App Password) to application-local.properties.");
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return new TestEmailResult(false, "JavaMailSender service is not available.");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(mailUsername, senderName));
            helper.setTo(testRecipient);
            helper.setSubject("[SkillSphere Nexus] SMTP Mailer Verification Test");
            helper.setText("<h3>SkillSphere Nexus SMTP Verification</h3><p>This test email confirms your Gmail SMTP connection is operational.</p>", true);
            mailSender.send(message);
            return new TestEmailResult(true, "Test email sent successfully to " + testRecipient + " via Gmail SMTP!");
        } catch (Exception ex) {
            return new TestEmailResult(false, "SMTP Error: " + ex.getMessage());
        }
    }

    public record TestEmailResult(boolean success, String message) {
    }

    private String buildHtmlScorecard(String recipientName, String assessmentTitle, String trackName,
            int attemptNumber, int maxAttempts, int earnedPoints, int maximumPoints,
            int percentage, int passPercentage, boolean passed, IssuedCertificateView certificate) {
        String badgeColor = passed ? "#10b981" : "#ef4444";
        String statusText = passed ? "PASSED &amp; CLEARED" : "NOT PASSED (RETRY AVAILABLE)";
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><body style=\"margin:0;padding:24px;background:#f8fafc;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;color:#0f172a;\">");
        sb.append("<div style=\"max-width:600px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e2e8f0;box-shadow:0 4px 6px -1px rgba(0,0,0,0.05);\">");
        sb.append("<div style=\"background:linear-gradient(135deg,#1e293b 0%,#0f172a 100%);padding:28px 32px;color:#ffffff;\">");
        sb.append("<div style=\"font-size:11px;font-weight:700;letter-spacing:1px;color:#60a5fa;text-transform:uppercase;\">SkillSphere Nexus Enterprise</div>");
        sb.append("<h1 style=\"margin:8px 0 0;font-size:22px;font-weight:700;\">Official Assessment Scorecard</h1>");
        sb.append("</div>");
        sb.append("<div style=\"padding:32px;\">");
        sb.append("<p style=\"font-size:15px;margin:0 0 20px;\">Dear <strong>").append(recipientName).append("</strong>,</p>");
        sb.append("<p style=\"font-size:14px;color:#475569;margin:0 0 24px;\">Your assessment submission has been evaluated. Below is your official performance scorecard:</p>");

        sb.append("<div style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:20px;margin-bottom:24px;\">");
        sb.append("<div style=\"display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;\">");
        sb.append("<span style=\"font-size:13px;font-weight:600;color:#64748b;\">Evaluation Outcome</span>");
        sb.append("<span style=\"background:").append(badgeColor).append(";color:#ffffff;font-size:12px;font-weight:700;padding:4px 12px;border-radius:20px;\">").append(statusText).append("</span>");
        sb.append("</div>");

        sb.append("<table style=\"width:100%;border-collapse:collapse;font-size:13.5px;\">");
        sb.append("<tr><td style=\"padding:8px 0;color:#64748b;border-bottom:1px solid #e2e8f0;\">Assessment:</td><td style=\"padding:8px 0;font-weight:600;text-align:right;border-bottom:1px solid #e2e8f0;\">").append(assessmentTitle).append("</td></tr>");
        sb.append("<tr><td style=\"padding:8px 0;color:#64748b;border-bottom:1px solid #e2e8f0;\">Course:</td><td style=\"padding:8px 0;font-weight:600;text-align:right;border-bottom:1px solid #e2e8f0;\">").append(trackName).append("</td></tr>");
        sb.append("<tr><td style=\"padding:8px 0;color:#64748b;border-bottom:1px solid #e2e8f0;\">Attempt:</td><td style=\"padding:8px 0;font-weight:600;text-align:right;border-bottom:1px solid #e2e8f0;\">").append(attemptNumber).append(" of ").append(maxAttempts).append("</td></tr>");
        sb.append("<tr><td style=\"padding:8px 0;color:#64748b;border-bottom:1px solid #e2e8f0;\">Marks Scored:</td><td style=\"padding:8px 0;font-weight:700;font-size:16px;color:#1e293b;text-align:right;border-bottom:1px solid #e2e8f0;\">").append(earnedPoints).append(" / ").append(maximumPoints).append("</td></tr>");
        sb.append("<tr><td style=\"padding:8px 0;color:#64748b;border-bottom:1px solid #e2e8f0;\">Final Score:</td><td style=\"padding:8px 0;font-weight:700;font-size:16px;color:").append(badgeColor).append(";text-align:right;border-bottom:1px solid #e2e8f0;\">").append(percentage).append("% (Pass: ").append(passPercentage).append("%)</td></tr>");
        if (certificate != null) {
            sb.append("<tr><td style=\"padding:8px 0;color:#64748b;\">Certificate #:</td><td style=\"padding:8px 0;font-weight:600;text-align:right;color:#2563eb;\">").append(certificate.certificateNumber()).append("</td></tr>");
        }
        sb.append("</table>");
        sb.append("</div>");

        if (passed) {
            sb.append("<p style=\"color:#059669;font-weight:600;font-size:14px;\">&check; Congratulations! Your certificate is available for download in your Student Portal.</p>");
        } else {
            sb.append("<p style=\"color:#b91c1c;font-size:13.5px;\">You may review the questions and domain concepts after completing all attempts or re-attempt to improve your score.</p>");
        }

        sb.append("</div>");
        sb.append("<div style=\"background:#f8fafc;padding:16px 32px;border-top:1px solid #e2e8f0;font-size:12px;color:#94a3b8;text-align:center;\">");
        sb.append("SkillSphere Nexus Automated Assessment Engine &bull; Automated notification - please do not reply directly.");
        sb.append("</div>");
        sb.append("</div></body></html>");
        return sb.toString();
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

