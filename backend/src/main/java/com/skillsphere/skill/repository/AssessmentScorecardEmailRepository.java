package com.skillsphere.skill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillsphere.skill.entity.AssessmentScorecardEmail;

@Repository
public interface AssessmentScorecardEmailRepository extends JpaRepository<AssessmentScorecardEmail, Long> {
    List<AssessmentScorecardEmail> findByStudentIdOrderBySentAtDesc(Long studentId);
    List<AssessmentScorecardEmail> findByEmployeeIdOrderBySentAtDesc(Long employeeId);
    List<AssessmentScorecardEmail> findByRecipientEmailIgnoreCaseOrderBySentAtDesc(String recipientEmail);
    List<AssessmentScorecardEmail> findAllByOrderBySentAtDesc();
}

