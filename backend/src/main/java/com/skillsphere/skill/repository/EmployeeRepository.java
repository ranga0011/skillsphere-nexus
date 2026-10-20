
package com.skillsphere.skill.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsphere.skill.entity.Employee;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);
        }