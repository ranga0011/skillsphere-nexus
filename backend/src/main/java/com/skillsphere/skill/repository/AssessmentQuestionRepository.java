package com.skillsphere.skill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsphere.skill.entity.AssessmentQuestion;

public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {
    @EntityGraph(attributePaths = "options")
    List<AssessmentQuestion> findByAssessment_IdOrderById(Long assessmentId);
}
