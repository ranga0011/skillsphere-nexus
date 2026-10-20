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
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Certification createCertification(@RequestBody Certification request) {
        return certificationService.createCertification(
                request.getEmployeeId(),
                request.getName(),
                request.getIssuingOrganization(),
                request.getCredentialId(),
                request.getIssueDate(),
                request.getExpiryDate(),
                request.getVerificationStatus());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public Certification updateCertification(@PathVariable Long id, @RequestBody Certification request) {
        return certificationService.updateCertification(
                id,
                request.getEmployeeId(),
                request.getName(),
                request.getIssuingOrganization(),
                request.getCredentialId(),
                request.getIssueDate(),
                request.getExpiryDate(),
                request.getVerificationStatus());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCertification(@PathVariable Long id) {
        certificationService.deleteCertification(id);
    }
}
