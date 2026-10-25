package com.skillsphere.skill.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "internal_jobs")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InternalJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Department is required")
    @Column(nullable = false, length = 150)
    private String department;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Required skills is required")
    @Column(name = "required_skills", nullable = false, length = 500)
    private String requiredSkills;

    @Column(name = "openings_count")
    private Integer openingsCount = 1;

    @Column(length = 50)
    private String status = "OPEN"; // OPEN, CLOSED

    public InternalJob() {}

    public InternalJob(String title, String department, String description, String requiredSkills, Integer openingsCount) {
        this.title = title;
        this.department = department;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.openingsCount = openingsCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public Integer getOpeningsCount() { return openingsCount; }
    public void setOpeningsCount(Integer openingsCount) { this.openingsCount = openingsCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
