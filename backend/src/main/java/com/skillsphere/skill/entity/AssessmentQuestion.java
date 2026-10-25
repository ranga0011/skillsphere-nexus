package com.skillsphere.skill.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "assessment_questions")
public class AssessmentQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private AssessmentDefinition assessment;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String prompt;

    @ElementCollection
    @CollectionTable(name = "assessment_question_options", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "option_order")
    @Column(name = "option_text", nullable = false, length = 500)
    private List<String> options = new ArrayList<>();

    @NotNull
    @Min(0)
    @Column(name = "correct_option_index", nullable = false)
    private Integer correctOptionIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentDifficulty difficulty;

    @Min(1)
    @Max(10)
    @Column(nullable = false)
    private int points = 1;

    public AssessmentQuestion() {
    }

    public Long getId() { return id; }
    public AssessmentDefinition getAssessment() { return assessment; }
    public void setAssessment(AssessmentDefinition assessment) { this.assessment = assessment; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = new ArrayList<>(options); }
    public Integer getCorrectOptionIndex() { return correctOptionIndex; }
    public void setCorrectOptionIndex(Integer correctOptionIndex) { this.correctOptionIndex = correctOptionIndex; }
    public AssessmentDifficulty getDifficulty() { return difficulty; }
    public void setDifficulty(AssessmentDifficulty difficulty) { this.difficulty = difficulty; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}
