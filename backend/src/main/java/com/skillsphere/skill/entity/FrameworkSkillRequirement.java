package com.skillsphere.skill.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "framework_skill_requirements",
    uniqueConstraints = @UniqueConstraint(columnNames = {"framework_id", "skill_id"})
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FrameworkSkillRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "framework_id", nullable = false)
    @NotNull(message = "Framework is required")
    @JsonIgnore
    private CompetencyFramework framework;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    @NotNull(message = "Skill is required")
    private Skill skill;

    @Min(value = 1, message = "Required proficiency must be between 1 and 10")
    @Max(value = 10, message = "Required proficiency must be between 1 and 10")
    @Column(name = "required_proficiency", nullable = false)
    private Integer requiredProficiency;

    @Column(length = 255)
    private String notes;

    @Transient
    private Long skillId;

    @Transient
    private Long frameworkId;

    public FrameworkSkillRequirement() {
    }

    public Long getId() {
        return id;
    }

    public CompetencyFramework getFramework() {
        return framework;
    }

    public void setFramework(CompetencyFramework framework) {
        this.framework = framework;
        if (framework != null) {
            this.frameworkId = framework.getId();
        }
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
        if (skill != null) {
            this.skillId = skill.getId();
        }
    }

    public Integer getRequiredProficiency() {
        return requiredProficiency;
    }

    public void setRequiredProficiency(Integer requiredProficiency) {
        this.requiredProficiency = requiredProficiency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getSkillId() {
        if (skillId != null) {
            return skillId;
        }
        return skill != null ? skill.getId() : null;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public Long getFrameworkId() {
        if (frameworkId != null) {
            return frameworkId;
        }
        return framework != null ? framework.getId() : null;
    }

    public void setFrameworkId(Long frameworkId) {
        this.frameworkId = frameworkId;
    }
}
