package com.skillsphere.skill.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "learner_assessment_attempts")
public class LearnerAssessmentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private AssessmentDefinition assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_assessment_id")
    private SkillAssessment skillAssessment;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LearnerAttemptStatus status = LearnerAttemptStatus.IN_PROGRESS;

    @Column(name = "score_points")
    private Integer scorePoints;

    @Column(name = "max_points")
    private Integer maxPoints;

    @Column(name = "percentage")
    private Integer percentage;

    @Column(nullable = false)
    private boolean passed;

    public LearnerAssessmentAttempt() {
    }

    public Long getId() { return id; }
    public AssessmentDefinition getAssessment() { return assessment; }
    public void setAssessment(AssessmentDefinition assessment) { this.assessment = assessment; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public SkillAssessment getSkillAssessment() { return skillAssessment; }
    public void setSkillAssessment(SkillAssessment skillAssessment) { this.skillAssessment = skillAssessment; }
    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public LearnerAttemptStatus getStatus() { return status; }
    public void setStatus(LearnerAttemptStatus status) { this.status = status; }
    public Integer getScorePoints() { return scorePoints; }
    public void setScorePoints(Integer scorePoints) { this.scorePoints = scorePoints; }
    public Integer getMaxPoints() { return maxPoints; }
    public void setMaxPoints(Integer maxPoints) { this.maxPoints = maxPoints; }
    public Integer getPercentage() { return percentage; }
    public void setPercentage(Integer percentage) { this.percentage = percentage; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
}
