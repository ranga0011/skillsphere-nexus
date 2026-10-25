package com.skillsphere.skill.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsphere.skill.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
