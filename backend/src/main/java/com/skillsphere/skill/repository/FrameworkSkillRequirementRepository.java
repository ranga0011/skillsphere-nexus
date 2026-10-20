package com.skillsphere.skill.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsphere.skill.entity.FrameworkSkillRequirement;

public interface FrameworkSkillRequirementRepository extends JpaRepository<FrameworkSkillRequirement, Long> {
    @EntityGraph(attributePaths = "skill")
    List<FrameworkSkillRequirement> findByFramework_Id(Long frameworkId);

    @EntityGraph(attributePaths = "skill")
    Optional<FrameworkSkillRequirement> findByFramework_IdAndSkill_Id(Long frameworkId, Long skillId);
}
