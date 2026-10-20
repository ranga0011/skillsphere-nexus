package com.skillsphere.skill.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsphere.skill.entity.SkillAssessment;
import com.skillsphere.skill.entity.VerificationStatus;

public interface SkillAssessmentRepository extends JpaRepository<SkillAssessment, Long> {
    @Override
    @EntityGraph(attributePaths = {"employee", "skill"})
    List<SkillAssessment> findAll();

    @Override
    @EntityGraph(attributePaths = {"employee", "skill"})
    Optional<SkillAssessment> findById(Long id);

    @EntityGraph(attributePaths = {"employee", "skill"})
    List<SkillAssessment> findByEmployee_Id(Long employeeId);

    @EntityGraph(attributePaths = {"employee", "skill"})
    List<SkillAssessment> findBySkill_Id(Long skillId);

    List<SkillAssessment> findByVerificationStatus(VerificationStatus verificationStatus);

    List<SkillAssessment> findByAssessmentDateBetween(LocalDate fromDate, LocalDate toDate);
}
