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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "learning_resources")
public class LearningResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String title;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    @NotNull
    private Skill skill;

    @NotBlank
    @Size(max = 30)
    @Column(name = "resource_type", nullable = false, length = 30)
    private String resourceType;

    @NotBlank
    @Size(max = 20)
    @Column(name = "difficulty_level", nullable = false, length = 20)
    private String difficultyLevel;

    @Size(max = 2048)
    @Column(name = "verified_url", length = 2048)
    private String verifiedUrl;

    @Column(nullable = false)
    private boolean active = true;

    public LearningResource() {
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public String getVerifiedUrl() { return verifiedUrl; }
    public void setVerifiedUrl(String verifiedUrl) { this.verifiedUrl = verifiedUrl; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
