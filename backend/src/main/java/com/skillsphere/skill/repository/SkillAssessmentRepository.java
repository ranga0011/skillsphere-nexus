package com.skillsphere.skill.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillsphere.skill.entity.SkillAssessment;
import com.skillsphere.skill.entity.VerificationStatus;

public interface SkillAssessmentRepository extends JpaRepository<SkillAssessment, Long> {
    @Override
    @EntityGraph(attributePaths = {"employee", "student", "skill"})
    List<SkillAssessment> findAll();

    @Override
    @EntityGraph(attributePaths = {"employee", "student", "skill"})
    Optional<SkillAssessment> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from SkillAssessment a where a.id = :id")
    Optional<SkillAssessment> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"employee", "student", "skill"})
    List<SkillAssessment> findByEmployee_Id(Long employeeId);

    @EntityGraph(attributePaths = {"student", "skill"})
    List<SkillAssessment> findByStudent_Id(Long studentId);

    Optional<SkillAssessment> findFirstByEmployee_IdAndSkill_IdAndVerificationStatusAndIdNotOrderByAssessmentDateDescIdDesc(
            Long employeeId, Long skillId, VerificationStatus verificationStatus, Long excludedAssessmentId);

    Optional<SkillAssessment> findFirstByStudent_IdAndSkill_IdAndVerificationStatusAndIdNotOrderByAssessmentDateDescIdDesc(
            Long studentId, Long skillId, VerificationStatus verificationStatus, Long excludedAssessmentId);

    @EntityGraph(attributePaths = {"employee", "student", "skill"})
    List<SkillAssessment> findBySkill_Id(Long skillId);

    List<SkillAssessment> findByVerificationStatus(VerificationStatus verificationStatus);

    List<SkillAssessment> findByAssessmentDateBetween(LocalDate fromDate, LocalDate toDate);
}
