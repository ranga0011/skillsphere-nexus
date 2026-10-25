package com.skillsphere.skill.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

@Entity
@Table(
    name = "employee_skill_mappings",
    check = @CheckConstraint(
        name = "ck_employee_skill_mapping_owner",
        constraint = "(employee_id IS NOT NULL AND student_id IS NULL) OR (employee_id IS NULL AND student_id IS NOT NULL)"
    ),
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "skill_id"}),
        @UniqueConstraint(columnNames = {"student_id", "skill_id"})
    }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EmployeeSkillMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_assessment_id", unique = true)
    private SkillAssessment verifiedAssessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    @NotNull(message = "Skill is required")
    private Skill skill;

    @Min(value = 1, message = "Proficiency must be between 1 and 10")
    @Max(value = 10, message = "Proficiency must be between 1 and 10")
    @Column(nullable = false)
    private Integer proficiency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING_VERIFICATION;

    @PastOrPresent(message = "Assessment date cannot be in the future")
    @Column(name = "assessment_date")
    private LocalDate assessmentDate;

    @Transient
    private Long employeeId;

    @Transient
    private Long studentId;

    @Transient
    private Long skillId;

    public EmployeeSkillMapping() {
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

    public SkillAssessment getVerifiedAssessment() {
        return verifiedAssessment;
    }

    public void setVerifiedAssessment(SkillAssessment verifiedAssessment) {
        this.verifiedAssessment = verifiedAssessment;
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

    public Integer getProficiency() {
        return proficiency;
    }

    public void setProficiency(Integer proficiency) {
        this.proficiency = proficiency;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus == null ? VerificationStatus.PENDING_VERIFICATION : verificationStatus;
    }

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
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
