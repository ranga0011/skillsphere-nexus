
package com.skillsphere.skill.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsphere.skill.entity.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}
