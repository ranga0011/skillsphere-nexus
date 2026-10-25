package com.skillsphere.skill.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "courses")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course title is required")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Duration hours is required")
    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours = 40;

    @Column(length = 150)
    private String instructor;

    @Column(name = "course_type", nullable = false, length = 50)
    private String courseType = "ONLINE"; // ONLINE, WORKSHOP, WEBINAR, BOOTCAMP

    @Column(precision = 3)
    private Double rating = 4.8;

    @Column(name = "total_enrolled")
    private Integer totalEnrolled = 0;

    @Column(name = "total_completed")
    private Integer totalCompleted = 0;

    @Column(length = 100)
    private String category;

    public Course() {}

    public Course(String title, String description, Integer durationHours, String instructor, String courseType, Double rating, String category) {
        this.title = title;
        this.description = description;
        this.durationHours = durationHours;
        this.instructor = instructor;
        this.courseType = courseType;
        this.rating = rating;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalEnrolled() { return totalEnrolled; }
    public void setTotalEnrolled(Integer totalEnrolled) { this.totalEnrolled = totalEnrolled; }

    public Integer getTotalCompleted() { return totalCompleted; }
    public void setTotalCompleted(Integer totalCompleted) { this.totalCompleted = totalCompleted; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
