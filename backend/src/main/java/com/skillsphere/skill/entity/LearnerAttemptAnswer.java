package com.skillsphere.skill.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "learner_attempt_answers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attempt_id", "question_id"}))
public class LearnerAttemptAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private LearnerAssessmentAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private AssessmentQuestion question;

    @Column(name = "selected_option_index", nullable = false)
    private int selectedOptionIndex;

    protected LearnerAttemptAnswer() {
    }

    public LearnerAttemptAnswer(LearnerAssessmentAttempt attempt, AssessmentQuestion question,
            int selectedOptionIndex) {
        this.attempt = attempt;
        this.question = question;
        this.selectedOptionIndex = selectedOptionIndex;
    }

    public Long getId() { return id; }
    public LearnerAssessmentAttempt getAttempt() { return attempt; }
    public AssessmentQuestion getQuestion() { return question; }
    public int getSelectedOptionIndex() { return selectedOptionIndex; }
}
