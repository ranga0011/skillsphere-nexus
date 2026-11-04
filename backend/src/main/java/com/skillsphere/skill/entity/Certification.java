package com.skillsphere.skill.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "certifications")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Certification name is required")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Issuing organization is required")
    @Column(name = "issuing_organization", nullable = false, length = 150)
    private String issuingOrganization;

    @Column(name = "credential_id", length = 120)
    private String credentialId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    @NotNull(message = "Issue date is required")
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING_VERIFICATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CertificationStatus status = CertificationStatus.ACTIVE;

    @Transient
    private Long employeeId;

    @Column(name = "document_name", length = 255)
    private String documentName;

    @Column(name = "document_type", length = 100)
    private String documentType;

    @Column(name = "document_data", columnDefinition = "TEXT")
    private String documentData;

    @Column(name = "authenticity_status", length = 50)
    private String authenticityStatus;

    @Column(name = "authenticity_score")
    private Integer authenticityScore;

    @Column(name = "verification_hash", length = 128)
    private String verificationHash;

    public Certification() {
    }

    @PrePersist
    @PreUpdate
    public void updateComputedStatus() {
        this.status = CertificationStatus.fromDates(this.expiryDate);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIssuingOrganization() {
        return issuingOrganization;
    }

    public void setIssuingOrganization(String issuingOrganization) {
        this.issuingOrganization = issuingOrganization;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
        if (employee != null) {
            this.employeeId = employee.getId();
        }
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus == null ? VerificationStatus.PENDING_VERIFICATION : verificationStatus;
    }

    public CertificationStatus getStatus() {
        return status;
    }

    public void setStatus(CertificationStatus status) {
        this.status = status;
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

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentData() {
        return documentData;
    }

    public void setDocumentData(String documentData) {
        this.documentData = documentData;
    }

    public String getAuthenticityStatus() {
        return authenticityStatus;
    }

    public void setAuthenticityStatus(String authenticityStatus) {
        this.authenticityStatus = authenticityStatus;
    }

    public Integer getAuthenticityScore() {
        return authenticityScore;
    }

    public void setAuthenticityScore(Integer authenticityScore) {
        this.authenticityScore = authenticityScore;
    }

    public String getVerificationHash() {
        return verificationHash;
    }

    public void setVerificationHash(String verificationHash) {
        this.verificationHash = verificationHash;
    }

}
