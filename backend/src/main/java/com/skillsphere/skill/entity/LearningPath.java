package com.skillsphere.skill.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "learning_paths")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_role", length = 150)
    private String targetRole;

    @Column(name = "total_courses")
    private Integer totalCourses = 1;

    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @Column(name = "courses_summary", length = 500)
    private String coursesSummary;

    public LearningPath() {}

    public LearningPath(String title, String description, String targetRole, Integer totalCourses, Integer progressPercentage, String coursesSummary) {
        this.title = title;
        this.description = description;
        this.targetRole = targetRole;
        this.totalCourses = totalCourses;
        this.progressPercentage = progressPercentage;
        this.coursesSummary = coursesSummary;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public Integer getTotalCourses() { return totalCourses; }
    public void setTotalCourses(Integer totalCourses) { this.totalCourses = totalCourses; }

    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }

    public String getCoursesSummary() { return coursesSummary; }
    public void setCoursesSummary(String coursesSummary) { this.coursesSummary = coursesSummary; }
}
