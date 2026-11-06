package com.skillsphere.skill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsphere.skill.entity.LearnerAttemptAnswer;

public interface LearnerAttemptAnswerRepository extends JpaRepository<LearnerAttemptAnswer, Long> {
    @EntityGraph(attributePaths = {"question", "attempt"})
    List<LearnerAttemptAnswer> findByAttempt_Id(Long attemptId);
}

