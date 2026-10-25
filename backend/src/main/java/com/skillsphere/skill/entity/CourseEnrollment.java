package com.skillsphere.skill.entity;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "course_enrollments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(nullable = false, length = 50)
    private String status = "ENROLLED"; // ENROLLED, IN_PROGRESS, COMPLETED

    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage = 0;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDate enrolledAt;

    @Column(name = "completed_at")
    private LocalDate completedAt;

    public CourseEnrollment() {
        this.enrolledAt = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }

    public LocalDate getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDate enrolledAt) { this.enrolledAt = enrolledAt; }

    public LocalDate getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDate completedAt) { this.completedAt = completedAt; }

    public Long getEmployeeId() {
        return employee != null ? employee.getId() : null;
    }

    public Long getStudentId() {
        return student != null ? student.getId() : null;
    }
}
