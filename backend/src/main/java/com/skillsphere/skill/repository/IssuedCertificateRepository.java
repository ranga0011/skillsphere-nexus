package com.skillsphere.skill.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsphere.skill.entity.IssuedCertificate;

public interface IssuedCertificateRepository extends JpaRepository<IssuedCertificate, Long> {
    @EntityGraph(attributePaths = {"employee", "student", "skill", "assessment"})
    List<IssuedCertificate> findByEmployee_Id(Long employeeId);

    @EntityGraph(attributePaths = {"employee", "student", "skill", "assessment"})
    List<IssuedCertificate> findByStudent_Id(Long studentId);

    @Override
    @EntityGraph(attributePaths = {"employee", "student", "skill", "assessment"})
    List<IssuedCertificate> findAll();

    @Override
    @EntityGraph(attributePaths = {"employee", "student", "skill", "assessment"})
    Optional<IssuedCertificate> findById(Long id);

    @EntityGraph(attributePaths = {"employee", "student", "skill", "assessment"})
    Optional<IssuedCertificate> findByAssessment_Id(Long assessmentId);

    @EntityGraph(attributePaths = {"skill"})
    Optional<IssuedCertificate> findByVerificationToken(String verificationToken);
}
