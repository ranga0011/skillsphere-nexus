package com.skillsphere.skill.entity;

import java.time.Instant;
import java.time.LocalDate;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "issued_certificates", uniqueConstraints = {
        @UniqueConstraint(columnNames = "certificate_number"),
        @UniqueConstraint(columnNames = "verification_token"),
        @UniqueConstraint(columnNames = "assessment_id")
}, check = @CheckConstraint(
        name = "ck_issued_certificate_owner",
        constraint = "(employee_id IS NOT NULL AND student_id IS NULL) OR (employee_id IS NULL AND student_id IS NOT NULL)"))
public class IssuedCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "certificate_number", nullable = false, unique = true, length = 80)
    private String certificateNumber;

    @Column(name = "verification_token", nullable = false, unique = true, length = 36)
    private String verificationToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false, unique = true)
    private SkillAssessment assessment;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssuedCertificateStatus status;

    @Column(name = "issuer_name", nullable = false, length = 150)
    private String issuerName;

    @Column(name = "proficiency_level", nullable = false)
    private Integer proficiencyLevel;

    @Column(name = "pdf_content", nullable = false, columnDefinition = "bytea")
    private byte[] pdfContent;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_by", length = 150)
    private String revokedBy;

    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public IssuedCertificate() {
    }

    @PrePersist
    @SuppressWarnings("unused")
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public SkillAssessment getAssessment() {
        return assessment;
    }

    public void setAssessment(SkillAssessment assessment) {
        this.assessment = assessment;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public IssuedCertificateStatus getStatus() {
        return status;
    }

    public void setStatus(IssuedCertificateStatus status) {
        this.status = status;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public Integer getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(Integer proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public byte[] getPdfContent() {
        return pdfContent;
    }

    public void setPdfContent(byte[] pdfContent) {
        this.pdfContent = pdfContent;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getRevokedBy() {
        return revokedBy;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void revoke(String username, String reason) {
        this.status = IssuedCertificateStatus.REVOKED;
        this.revokedAt = Instant.now();
        this.revokedBy = username;
        this.revocationReason = reason;
    }
}
