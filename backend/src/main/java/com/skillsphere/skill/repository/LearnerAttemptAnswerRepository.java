package com.skillsphere.skill.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsphere.skill.entity.LearnerAttemptAnswer;

public interface LearnerAttemptAnswerRepository extends JpaRepository<LearnerAttemptAnswer, Long> {
}
