package com.skillsphere.skill.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_scorecard_emails")
public class AssessmentScorecardEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id")
    private Long attemptId;

    @Column(name = "assessment_id")
    private Long assessmentId;

    @Column(name = "assessment_title", length = 200)
    private String assessmentTitle;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "recipient_email", length = 150, nullable = false)
    private String recipientEmail;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "score_points")
    private int scorePoints;

    @Column(name = "max_points")
    private int maxPoints;

    @Column(name = "percentage")
    private int percentage;

    @Column(name = "passed")
    private boolean passed;

    @Column(name = "subject", length = 300)
    private String subject;

    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    @Column(name = "status", length = 50)
    private String status = "DELIVERED";

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    public AssessmentScorecardEmail() {
    }

    public AssessmentScorecardEmail(Long attemptId, Long assessmentId, String assessmentTitle,
            Long studentId, Long employeeId, String recipientEmail, String recipientName,
            int scorePoints, int maxPoints, int percentage, boolean passed,
            String subject, String bodyText) {
        this.attemptId = attemptId;
        this.assessmentId = assessmentId;
        this.assessmentTitle = assessmentTitle;
        this.studentId = studentId;
        this.employeeId = employeeId;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.scorePoints = scorePoints;
        this.maxPoints = maxPoints;
        this.percentage = percentage;
        this.passed = passed;
        this.subject = subject;
        this.bodyText = bodyText;
        this.sentAt = Instant.now();
        this.status = "DELIVERED";
    }

    @PrePersist
    void onPrePersist() {
        if (sentAt == null) {
            sentAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }
    public String getAssessmentTitle() { return assessmentTitle; }
    public void setAssessmentTitle(String assessmentTitle) { this.assessmentTitle = assessmentTitle; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public int getScorePoints() { return scorePoints; }
    public void setScorePoints(int scorePoints) { this.scorePoints = scorePoints; }
    public int getMaxPoints() { return maxPoints; }
    public void setMaxPoints(int maxPoints) { this.maxPoints = maxPoints; }
    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}

