package com.skillsphere.skill.entity;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "job_applications")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private InternalJob job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "applied_at", nullable = false)
    private LocalDate appliedAt;

    @Column(name = "match_percentage")
    private Integer matchPercentage = 0;

    @Column(length = 50)
    private String status = "APPLIED"; // APPLIED, UNDER_REVIEW, OFFERED, REJECTED

    public JobApplication() {
        this.appliedAt = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InternalJob getJob() { return job; }
    public void setJob(InternalJob job) { this.job = job; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public LocalDate getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDate appliedAt) { this.appliedAt = appliedAt; }

    public Integer getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(Integer matchPercentage) { this.matchPercentage = matchPercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getEmployeeId() {
        return employee != null ? employee.getId() : null;
    }

    public Long getStudentId() {
        return student != null ? student.getId() : null;
    }
}
