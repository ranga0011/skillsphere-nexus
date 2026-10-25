package com.skillsphere.skill.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.skillsphere.skill.entity.CareerPlan;

public interface CareerPlanRepository extends JpaRepository<CareerPlan, Long> {

    @Query("SELECT c FROM CareerPlan c WHERE c.employee.id = :employeeId")
    List<CareerPlan> findByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT c FROM CareerPlan c WHERE c.student.id = :studentId")
    List<CareerPlan> findByStudentId(@Param("studentId") Long studentId);

    Optional<CareerPlan> findFirstByEmployee_IdOrderByIdDesc(Long employeeId);

    Optional<CareerPlan> findFirstByStudent_IdOrderByIdDesc(Long studentId);

    default Optional<CareerPlan> findFirstByEmployeeIdOrderByIdDesc(Long employeeId) {
        return findFirstByEmployee_IdOrderByIdDesc(employeeId);
    }

    default Optional<CareerPlan> findFirstByStudentIdOrderByIdDesc(Long studentId) {
        return findFirstByStudent_IdOrderByIdDesc(studentId);
    }
}
