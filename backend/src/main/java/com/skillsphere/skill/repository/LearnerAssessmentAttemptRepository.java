package com.skillsphere.skill.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillsphere.skill.entity.LearnerAssessmentAttempt;

public interface LearnerAssessmentAttemptRepository extends JpaRepository<LearnerAssessmentAttempt, Long> {
    long countByAssessment_IdAndStudent_Id(Long assessmentId, Long studentId);
    long countByAssessment_IdAndEmployee_Id(Long assessmentId, Long employeeId);
    boolean existsByAssessment_Id(Long assessmentId);

    @EntityGraph(attributePaths = {"assessment", "assessment.skill"})
    List<LearnerAssessmentAttempt> findByStudent_IdOrderByStartedAtDesc(Long studentId);

    @EntityGraph(attributePaths = {"assessment", "assessment.skill"})
    List<LearnerAssessmentAttempt> findByEmployee_IdOrderByStartedAtDesc(Long employeeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from LearnerAssessmentAttempt a where a.id = :id")
    Optional<LearnerAssessmentAttempt> findByIdForUpdate(@Param("id") Long id);
}
