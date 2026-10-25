package com.skillsphere.skill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsphere.skill.entity.LearningResource;

public interface LearningResourceRepository extends JpaRepository<LearningResource, Long> {
    List<LearningResource> findByActiveTrueAndSkill_IdInOrderBySkill_NameAscTitleAsc(List<Long> skillIds);
    List<LearningResource> findByActiveTrueOrderByTitleAsc();
}
