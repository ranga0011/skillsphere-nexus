package com.skillsphere.skill.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsphere.skill.entity.EmployeeSkillMapping;
import com.skillsphere.skill.entity.VerificationStatus;

public interface EmployeeSkillMappingRepository extends JpaRepository<EmployeeSkillMapping, Long> {
    @Override
    @EntityGraph(attributePaths = {"employee", "student", "skill"})
    List<EmployeeSkillMapping> findAll();

    @Override
    @EntityGraph(attributePaths = {"employee", "student", "skill"})
    Optional<EmployeeSkillMapping> findById(Long id);

    @EntityGraph(attributePaths = {"employee", "student", "skill"})
    List<EmployeeSkillMapping> findByEmployee_Id(Long employeeId);

    @EntityGraph(attributePaths = {"student", "skill"})
    List<EmployeeSkillMapping> findByStudent_Id(Long studentId);

    @EntityGraph(attributePaths = {"employee", "student", "skill"})
    List<EmployeeSkillMapping> findBySkill_Id(Long skillId);

    boolean existsByEmployee_IdAndSkill_Id(Long employeeId, Long skillId);

    boolean existsByStudent_IdAndSkill_Id(Long studentId, Long skillId);

    @EntityGraph(attributePaths = {"employee", "student", "skill"})
    Optional<EmployeeSkillMapping> findByEmployee_IdAndSkill_Id(Long employeeId, Long skillId);

    @EntityGraph(attributePaths = {"employee", "student", "skill"})
    Optional<EmployeeSkillMapping> findByStudent_IdAndSkill_Id(Long studentId, Long skillId);

    List<EmployeeSkillMapping> findByVerificationStatus(VerificationStatus verificationStatus);
}
