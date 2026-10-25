package com.skillsphere.skill.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.skillsphere.skill.entity.LearningPath;

public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
}
