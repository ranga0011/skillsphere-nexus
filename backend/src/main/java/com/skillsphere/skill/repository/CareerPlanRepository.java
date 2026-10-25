package com.skillsphere.skill.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.skillsphere.skill.entity.CareerPlan;

public interface CareerPlanRepository extends JpaRepository<CareerPlan, Long> {
    List<CareerPlan> findByEmployeeId(Long employeeId);
    List<CareerPlan> findByStudentId(Long studentId);
    Optional<CareerPlan> findFirstByEmployeeIdOrderByIdDesc(Long employeeId);
    Optional<CareerPlan> findFirstByStudentIdOrderByIdDesc(Long studentId);
}
