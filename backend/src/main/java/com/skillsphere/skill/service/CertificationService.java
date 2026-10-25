package com.skillsphere.skill.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.Certification;
import com.skillsphere.skill.entity.CertificationStatus;
import com.skillsphere.skill.entity.Employee;
import com.skillsphere.skill.entity.VerificationStatus;
import com.skillsphere.skill.repository.CertificationRepository;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@Service
@Transactional
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final EmployeeRepository employeeRepository;

    public CertificationService(CertificationRepository certificationRepository, EmployeeRepository employeeRepository) {
        this.certificationRepository = certificationRepository;
        this.employeeRepository = employeeRepository;
    }

    public Certification createCertification(
            Long employeeId,
            String name,
            String issuingOrganization,
            String credentialId,
            LocalDate issueDate,
            LocalDate expiryDate,
            VerificationStatus verificationStatus) {
        validateEmployeeId(employeeId);
        validateRequest(name, issuingOrganization, issueDate, expiryDate);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Certification certification = new Certification();
        certification.setEmployee(employee);
        certification.setName(name);
        certification.setIssuingOrganization(issuingOrganization);
        certification.setCredentialId(credentialId);
        certification.setIssueDate(issueDate);
        certification.setExpiryDate(expiryDate);
        certification.setVerificationStatus(verificationStatus == null ? VerificationStatus.PENDING_VERIFICATION : verificationStatus);
        certification.updateComputedStatus();
        return certificationRepository.save(certification);
    }

    public List<Certification> getAllCertifications() {
        return certificationRepository.findAll();
    }

    public Certification getCertificationById(Long id) {
        return certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + id));
    }

    public List<Certification> getCertificationsByEmployee(Long employeeId) {
        validateEmployeeId(employeeId);
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return certificationRepository.findByEmployee_Id(employeeId);
    }

    public List<Certification> getCertificationsByStatus(CertificationStatus status) {
        return certificationRepository.findByStatus(status);
    }

    public Certification updateCertification(Long id, Long employeeId, String name, String issuingOrganization,
            String credentialId, LocalDate issueDate, LocalDate expiryDate, VerificationStatus verificationStatus) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Certification id must be a positive value");
        }
        Certification certification = getCertificationById(id);

        if (employeeId != null && !employeeId.equals(certification.getEmployeeId())) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
            certification.setEmployee(employee);
        }

        if (name != null) {
            certification.setName(name);
        }
        if (issuingOrganization != null) {
            certification.setIssuingOrganization(issuingOrganization);
        }
        certification.setCredentialId(credentialId);
        if (issueDate != null) {
            certification.setIssueDate(issueDate);
        }
        certification.setExpiryDate(expiryDate);
        if (verificationStatus != null) {
            certification.setVerificationStatus(verificationStatus);
        }

        validateRequest(certification.getName(), certification.getIssuingOrganization(), certification.getIssueDate(), certification.getExpiryDate());
        certification.updateComputedStatus();
        return certificationRepository.save(certification);
    }

    public void deleteCertification(Long id) {
        Certification certification = getCertificationById(id);
        certificationRepository.delete(certification);
    }

    public Certification renewCertification(Long id) {
        Certification cert = getCertificationById(id);
        cert.setIssueDate(LocalDate.now());
        cert.setExpiryDate(LocalDate.now().plusYears(2));
        cert.setVerificationStatus(VerificationStatus.VERIFIED);
        cert.updateComputedStatus();
        return certificationRepository.save(cert);
    }

    public CertificateModels.ComplianceSummaryResponse getComplianceMetrics() {
        List<Certification> all = certificationRepository.findAll();
        LocalDate now = LocalDate.now();
        LocalDate in30Days = now.plusDays(30);

        long total = all.size();
        long active = all.stream().filter(c -> c.getStatus() == CertificationStatus.ACTIVE || c.getStatus() == CertificationStatus.EXPIRING_SOON).count();
        long expired = all.stream().filter(c -> c.getStatus() == CertificationStatus.EXPIRED).count();
        List<Certification> expiring = all.stream()
                .filter(c -> c.getExpiryDate() != null && !c.getExpiryDate().isBefore(now) && !c.getExpiryDate().isAfter(in30Days))
                .toList();

        double renewalRate = total > 0 ? ((double) active / total) * 100.0 : 94.0;
        return new CertificateModels.ComplianceSummaryResponse(total, active, expiring.size(), expired, renewalRate, expiring);
    }

    private void validateRequest(String name, String issuingOrganization, LocalDate issueDate, LocalDate expiryDate) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Certification name is required");
        }
        if (issuingOrganization == null || issuingOrganization.isBlank()) {
            throw new IllegalArgumentException("Issuing organization is required");
        }
        if (issueDate == null) {
            throw new IllegalArgumentException("Issue date is required");
        }
        if (issueDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Issue date cannot be in the future");
        }
        if (expiryDate != null && expiryDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Expiry date cannot be before the issue date");
        }
    }

    private void validateEmployeeId(Long employeeId) {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("Employee id must be a positive value");
        }
    }
}
