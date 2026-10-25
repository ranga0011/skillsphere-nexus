package com.skillsphere.skill.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillsphere.skill.entity.AssessmentDefinition;

public interface AssessmentDefinitionRepository extends JpaRepository<AssessmentDefinition, Long> {
    @EntityGraph(attributePaths = "skill")
    List<AssessmentDefinition> findByActiveTrueOrderByTitleAsc();

    @Override
    @EntityGraph(attributePaths = "skill")
    Optional<AssessmentDefinition> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AssessmentDefinition a where a.id = :id")
    Optional<AssessmentDefinition> findByIdForUpdate(@Param("id") Long id);
}
