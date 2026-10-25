package com.skillsphere.skill.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CheckConstraint;
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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

@Entity
@Table(name = "skill_assessments", check = @CheckConstraint(
        name = "ck_skill_assessment_owner",
        constraint = "(employee_id IS NOT NULL AND student_id IS NULL) OR (employee_id IS NULL AND student_id IS NOT NULL)"))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SkillAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    @NotNull(message = "Skill is required")
    private Skill skill;

    @Min(value = 0, message = "Score must be zero or greater")
    @Column(nullable = false)
    private Integer score;

    @Min(value = 1, message = "Maximum score must be greater than zero")
    @Max(value = 1000, message = "Maximum score cannot exceed 1000")
    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    @PastOrPresent(message = "Assessment date cannot be in the future")
    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @NotBlank(message = "Assessment method is required")
    @Column(name = "assessment_method", nullable = false)
    private String assessmentMethod;

    @NotNull(message = "Verification status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING_VERIFICATION;

    @Column(name = "assessor_name")
    private String assessorName;

    @Transient
    private Long employeeId;

    @Transient
    private Long studentId;

    @Transient
    private Long skillId;

    public SkillAssessment() {
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
        if (employee == null) {
            this.employeeId = null;
        }
        if (employee != null) {
            this.employeeId = employee.getId();
            this.student = null;
            this.studentId = null;
        }
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
        if (student == null) {
            this.studentId = null;
        }
        if (student != null) {
            this.studentId = student.getId();
            this.employee = null;
            this.employeeId = null;
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public String getAssessmentMethod() {
        return assessmentMethod;
    }

    public void setAssessmentMethod(String assessmentMethod) {
        this.assessmentMethod = assessmentMethod;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus == null ? VerificationStatus.PENDING_VERIFICATION : verificationStatus;
    }

    public String getAssessorName() {
        return assessorName;
    }

    public void setAssessorName(String assessorName) {
        this.assessorName = assessorName;
    }

    public Long getEmployeeId() {
        if (employeeId != null) {
            return employeeId;
        }
        return employee != null ? employee.getId() : null;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getStudentId() {
        if (studentId != null) {
            return studentId;
        }
        return student != null ? student.getId() : null;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
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
}
