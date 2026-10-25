package com.skillsphere.skill.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.skillsphere.skill.entity.Certification;
import com.skillsphere.skill.entity.CertificationStatus;
import com.skillsphere.skill.service.CertificationService;
import com.skillsphere.skill.security.AuthorizationService;

@RestController
@RequestMapping("/api/certifications")
public class CertificationController {

    private final CertificationService certificationService;
    private final AuthorizationService authorizationService;

    public CertificationController(CertificationService certificationService,
            AuthorizationService authorizationService) {
        this.certificationService = certificationService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<Certification> getAllCertifications(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) CertificationStatus status) {
        if (employeeId != null) {
            authorizationService.requireEmployeeAccess(employeeId);
            return certificationService.getCertificationsByEmployee(employeeId);
        }
        if (authorizationService.isEmployee()) {
            return certificationService.getCertificationsByEmployee(authorizationService.getCurrentEmployeeId());
        }
        if (!authorizationService.isHrAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Employee filter is required");
        }
        if (status != null) {
            return certificationService.getCertificationsByStatus(status);
        }
        return certificationService.getAllCertifications();
    }

    @GetMapping("/{id}")
    public Certification getCertificationById(@PathVariable Long id) {
        Certification certification = certificationService.getCertificationById(id);
        authorizationService.requireEmployeeAccess(certification.getEmployeeId());
        return certification;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Certification createCertification(@RequestBody Certification request) {
        Long targetEmployeeId;
        com.skillsphere.skill.entity.VerificationStatus verificationStatus;
        if (authorizationService.isEmployee()) {
            targetEmployeeId = authorizationService.getCurrentEmployeeId();
            verificationStatus = com.skillsphere.skill.entity.VerificationStatus.PENDING_VERIFICATION;
        } else if (authorizationService.isHrAdmin()) {
            targetEmployeeId = request.getEmployeeId();
            verificationStatus = request.getVerificationStatus() != null ? request.getVerificationStatus() : com.skillsphere.skill.entity.VerificationStatus.VERIFIED;
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Must be an authenticated employee or HR admin to add certifications");
        }

        return certificationService.createCertification(
                targetEmployeeId,
                request.getName(),
                request.getIssuingOrganization(),
                request.getCredentialId(),
                request.getIssueDate(),
                request.getExpiryDate(),
                verificationStatus);
    }

    @PutMapping("/{id}")
    public Certification updateCertification(@PathVariable Long id, @RequestBody Certification request) {
        Certification existing = certificationService.getCertificationById(id);
        Long targetEmployeeId = existing.getEmployeeId();
        com.skillsphere.skill.entity.VerificationStatus verificationStatus = existing.getVerificationStatus();

        if (authorizationService.isEmployee()) {
            authorizationService.requireEmployeeAccess(existing.getEmployeeId());
            verificationStatus = com.skillsphere.skill.entity.VerificationStatus.PENDING_VERIFICATION;
        } else if (authorizationService.isHrAdmin()) {
            targetEmployeeId = request.getEmployeeId() != null ? request.getEmployeeId() : existing.getEmployeeId();
            verificationStatus = request.getVerificationStatus() != null ? request.getVerificationStatus() : existing.getVerificationStatus();
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }

        return certificationService.updateCertification(
                id,
                targetEmployeeId,
                request.getName(),
                request.getIssuingOrganization(),
                request.getCredentialId(),
                request.getIssueDate(),
                request.getExpiryDate(),
                verificationStatus);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCertification(@PathVariable Long id) {
        Certification existing = certificationService.getCertificationById(id);
        if (authorizationService.isEmployee()) {
            authorizationService.requireEmployeeAccess(existing.getEmployeeId());
        } else if (!authorizationService.isHrAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        certificationService.deleteCertification(id);
    }

    @GetMapping("/compliance-metrics")
    public com.skillsphere.skill.service.CertificateModels.ComplianceSummaryResponse getComplianceMetrics() {
        return certificationService.getComplianceMetrics();
    }

    @PostMapping("/{id}/renew")
    public Certification renewCertification(@PathVariable Long id) {
        Certification cert = certificationService.getCertificationById(id);
        authorizationService.requireEmployeeAccess(cert.getEmployeeId());
        return certificationService.renewCertification(id);
    }
}
