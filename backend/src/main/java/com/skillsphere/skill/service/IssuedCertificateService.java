package com.skillsphere.skill.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.IssuedCertificate;
import com.skillsphere.skill.entity.IssuedCertificateStatus;
import com.skillsphere.skill.entity.SkillAssessment;
import com.skillsphere.skill.entity.VerificationStatus;
import com.skillsphere.skill.repository.IssuedCertificateRepository;
import com.skillsphere.skill.service.CertificateModels.IssuedCertificateView;
import com.skillsphere.skill.service.CertificateModels.PublicCertificateVerification;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@Service
@Transactional
public class IssuedCertificateService {

    private final IssuedCertificateRepository certificateRepository;
    private final CertificatePdfGenerator pdfGenerator;
    private final String issuerName;
    private final int passingPercentage;

    public IssuedCertificateService(
            IssuedCertificateRepository certificateRepository,
            CertificatePdfGenerator pdfGenerator,
            @Value("${app.certificates.issuer:SkillSphere Nexus}") String issuerName,
            @Value("${app.certificates.passing-percentage:70}") int passingPercentage) {
        if (passingPercentage < 1 || passingPercentage > 100) {
            throw new IllegalArgumentException("Certificate passing percentage must be between 1 and 100");
        }
        this.certificateRepository = certificateRepository;
        this.pdfGenerator = pdfGenerator;
        this.issuerName = issuerName;
        this.passingPercentage = passingPercentage;
    }

    public IssuedCertificate issueIfEligible(SkillAssessment assessment) {
        if (assessment.getVerificationStatus() != VerificationStatus.VERIFIED
                || assessment.getScore() == null || assessment.getMaxScore() == null
                || assessment.getMaxScore() <= 0
                || assessment.getScore() * 100.0 / assessment.getMaxScore() < passingPercentage) {
            return null;
        }
        if (assessment.getEmployee() == null && assessment.getStudent() == null) {
            return null;
        }
        return certificateRepository.findByAssessment_Id(assessment.getId())
                .orElseGet(() -> createCertificate(assessment));
    }

    @Transactional(readOnly = true)
    public List<IssuedCertificateView> getForEmployee(Long employeeId) {
        return certificateRepository.findByEmployee_Id(employeeId).stream()
                .map(IssuedCertificateView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<IssuedCertificateView> getForStudent(Long studentId) {
        return certificateRepository.findByStudent_Id(studentId).stream()
                .map(IssuedCertificateView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<IssuedCertificateView> getAll() {
        return certificateRepository.findAll().stream().map(IssuedCertificateView::from).toList();
    }

    @Transactional(readOnly = true)
    public IssuedCertificate getById(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public IssuedCertificateView getDetails(Long id) {
        return IssuedCertificateView.from(getById(id));
    }

    @Transactional(readOnly = true)
    public boolean hasCertificateForAssessment(Long assessmentId) {
        return certificateRepository.findByAssessment_Id(assessmentId).isPresent();
    }

    @Transactional(readOnly = true)
    public Optional<IssuedCertificateView> getForAssessment(Long assessmentId) {
        return certificateRepository.findByAssessment_Id(assessmentId).map(IssuedCertificateView::from);
    }

    @Transactional(readOnly = true)
    public PublicCertificateVerification verify(String token) {
        IssuedCertificate certificate = certificateRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate verification record was not found"));
        return PublicCertificateVerification.from(certificate);
    }

    public IssuedCertificate revoke(Long id, String username, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A revocation reason is required");
        }
        IssuedCertificate certificate = getById(id);
        if (certificate.getStatus() == IssuedCertificateStatus.REVOKED) {
            return certificate;
        }
        certificate.revoke(username, reason.trim());
        return certificateRepository.save(certificate);
    }

    public void revokeForAssessment(Long assessmentId, String username, String reason) {
        certificateRepository.findByAssessment_Id(assessmentId)
                .ifPresent(certificate -> revoke(certificate.getId(), username, reason));
    }

    private IssuedCertificate createCertificate(SkillAssessment assessment) {
        int level = Math.max(1, Math.min(10,
                (int) Math.ceil(assessment.getScore() * 10.0 / assessment.getMaxScore())));
        String number = "SSN-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase(java.util.Locale.ROOT);
        String token = UUID.randomUUID().toString();
        String learnerName = assessment.getStudent() != null
                ? assessment.getStudent().getName()
                : assessment.getEmployee().getName();
        LocalDate issueDate = LocalDate.now();

        IssuedCertificate certificate = new IssuedCertificate();
        certificate.setCertificateNumber(number);
        certificate.setVerificationToken(token);
        certificate.setEmployee(assessment.getEmployee());
        certificate.setStudent(assessment.getStudent());
        certificate.setSkill(assessment.getSkill());
        certificate.setAssessment(assessment);
        certificate.setIssueDate(issueDate);
        certificate.setStatus(IssuedCertificateStatus.ISSUED);
        certificate.setIssuerName(issuerName);
        certificate.setProficiencyLevel(level);
        certificate.setPdfContent(pdfGenerator.generate(
                learnerName, assessment.getSkill().getName(), assessment.getSkill().getCategory(),
                issuerName, number, token, level, assessment, issueDate));
        return certificateRepository.save(certificate);
    }
}
