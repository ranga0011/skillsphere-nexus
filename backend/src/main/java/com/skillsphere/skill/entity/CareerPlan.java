package com.skillsphere.skill.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "career_plans")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CareerPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @NotBlank(message = "Current role is required")
    @Column(name = "current_role_title", nullable = false, length = 150)
    private String currentRole;

    @NotBlank(message = "Target role is required")
    @Column(name = "target_role", nullable = false, length = 150)
    private String targetRole;

    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @Column(name = "skill_gaps", length = 500)
    private String skillGaps;

    @Column(name = "mentor_name", length = 150)
    private String mentorName;

    @Column(name = "promotion_readiness_score")
    private Integer promotionReadinessScore = 0;

    @Column(name = "promotion_eligibility", length = 150)
    private String promotionEligibility;

    @Column(length = 50)
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, ON_HOLD

    public CareerPlan() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getCurrentRole() { return currentRole; }
    public void setCurrentRole(String currentRole) { this.currentRole = currentRole; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }

    public String getSkillGaps() { return skillGaps; }
    public void setSkillGaps(String skillGaps) { this.skillGaps = skillGaps; }

    public String getMentorName() { return mentorName; }
    public void setMentorName(String mentorName) { this.mentorName = mentorName; }

    public Integer getPromotionReadinessScore() { return promotionReadinessScore; }
    public void setPromotionReadinessScore(Integer promotionReadinessScore) { this.promotionReadinessScore = promotionReadinessScore; }

    public String getPromotionEligibility() { return promotionEligibility; }
    public void setPromotionEligibility(String promotionEligibility) { this.promotionEligibility = promotionEligibility; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getEmployeeId() {
        return employee != null ? employee.getId() : null;
    }

    public Long getStudentId() {
        return student != null ? student.getId() : null;
    }
}
