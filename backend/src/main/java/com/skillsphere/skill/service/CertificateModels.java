package com.skillsphere.skill.service;

import java.time.LocalDate;

import com.skillsphere.skill.entity.IssuedCertificate;
import com.skillsphere.skill.entity.IssuedCertificateStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CertificateModels {
    private CertificateModels() {
    }

    public record IssuedCertificateView(
            Long id,
            String certificateNumber,
            String learnerName,
            String skillName,
            String issuerName,
            LocalDate issueDate,
            IssuedCertificateStatus status,
            Integer proficiencyLevel,
            String verificationToken,
            Integer assessmentScore,
            Integer assessmentMaxScore) {
        static IssuedCertificateView from(IssuedCertificate certificate) {
            var assessment = certificate.getAssessment();
            String learnerName = certificate.getStudent() != null
                    ? certificate.getStudent().getName()
                    : certificate.getEmployee().getName();
            return new IssuedCertificateView(
                    certificate.getId(),
                    certificate.getCertificateNumber(),
                    learnerName,
                    certificate.getSkill().getName(),
                    certificate.getIssuerName(),
                    certificate.getIssueDate(),
                    certificate.getStatus(),
                    certificate.getProficiencyLevel(),
                    certificate.getVerificationToken(),
                    assessment.getScore(),
                    assessment.getMaxScore());
        }
    }

    public record PublicCertificateVerification(
            String certificateNumber,
            String skillName,
            String issuerName,
            LocalDate issueDate,
            IssuedCertificateStatus status) {
        static PublicCertificateVerification from(IssuedCertificate certificate) {
            return new PublicCertificateVerification(certificate.getCertificateNumber(),
                    certificate.getSkill().getName(), certificate.getIssuerName(),
                    certificate.getIssueDate(), certificate.getStatus());
        }
    }

    public record RevokeCertificateRequest(@NotBlank @Size(max = 500) String reason) {
    }
}
