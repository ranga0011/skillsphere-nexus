package com.skillsphere.skill.controller;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.skillsphere.skill.entity.IssuedCertificate;
import com.skillsphere.skill.entity.IssuedCertificateStatus;
import com.skillsphere.skill.security.AuthorizationService;
import com.skillsphere.skill.security.JwtService;
import com.skillsphere.skill.service.CertificateModels.IssuedCertificateView;
import com.skillsphere.skill.service.CertificateModels.PublicCertificateVerification;
import com.skillsphere.skill.service.CertificateModels.RevokeCertificateRequest;
import com.skillsphere.skill.service.IssuedCertificateService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/certificates")
public class IssuedCertificateController {

    private final IssuedCertificateService certificateService;
    private final AuthorizationService authorizationService;

    public IssuedCertificateController(IssuedCertificateService certificateService,
            AuthorizationService authorizationService) {
        this.certificateService = certificateService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<IssuedCertificateView> getCertificates(@AuthenticationPrincipal Jwt jwt) {
        if (authorizationService.isHrAdmin()) {
            return certificateService.getAll();
        }
        String role = jwt.getClaimAsString("role");
        if ("STUDENT".equals(role)) {
            Long studentId = JwtService.studentIdFromClaims(jwt.getClaims());
            authorizationService.requireStudentAccess(studentId);
            return certificateService.getForStudent(studentId);
        }
        if ("EMPLOYEE".equals(role)) {
            Long employeeId = JwtService.employeeIdFromClaims(jwt.getClaims());
            authorizationService.requireEmployeeAccess(employeeId);
            return certificateService.getForEmployee(employeeId);
        }
        throw new AccessDeniedException("A linked employee or student profile is required");
    }

    @GetMapping("/{id}")
    public IssuedCertificateView getCertificate(@PathVariable Long id) {
        IssuedCertificate certificate = certificateService.getById(id);
        authorizationService.requireProfileAccess(
                certificate.getEmployee() == null ? null : certificate.getEmployee().getId(),
                certificate.getStudent() == null ? null : certificate.getStudent().getId());
        return certificateService.getDetails(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) {
        IssuedCertificate certificate = certificateService.getById(id);
        authorizationService.requireProfileAccess(
                certificate.getEmployee() == null ? null : certificate.getEmployee().getId(),
                certificate.getStudent() == null ? null : certificate.getStudent().getId());
        if (certificate.getStatus() != IssuedCertificateStatus.ISSUED) {
            throw new ResponseStatusException(HttpStatus.GONE, "This certificate is no longer valid");
        }
        String fileName = "SkillSphere-Certificate-" + certificate.getCertificateNumber() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .body(certificate.getPdfContent());
    }

    @GetMapping("/verify/{token}")
    public PublicCertificateVerification verifyCertificate(@PathVariable String token) {
        return certificateService.verify(token);
    }

    @PutMapping("/{id}/revoke")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public IssuedCertificateView revokeCertificate(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RevokeCertificateRequest request) {
        IssuedCertificate certificate = certificateService.revoke(id, jwt.getSubject(), request.reason());
        return certificateService.getDetails(certificate.getId());
    }
}
