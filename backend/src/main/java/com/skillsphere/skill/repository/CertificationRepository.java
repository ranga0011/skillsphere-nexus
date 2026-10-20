package com.skillsphere.skill.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsphere.skill.entity.Certification;
import com.skillsphere.skill.entity.CertificationStatus;
import com.skillsphere.skill.entity.VerificationStatus;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    @Override
    @EntityGraph(attributePaths = "employee")
    List<Certification> findAll();

    @Override
    @EntityGraph(attributePaths = "employee")
    Optional<Certification> findById(Long id);

    @EntityGraph(attributePaths = "employee")
    List<Certification> findByEmployee_Id(Long employeeId);

    @EntityGraph(attributePaths = "employee")
    List<Certification> findByStatus(CertificationStatus status);

    List<Certification> findByVerificationStatus(VerificationStatus verificationStatus);
}
