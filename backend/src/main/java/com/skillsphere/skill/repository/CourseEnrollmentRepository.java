package com.skillsphere.skill.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.skillsphere.skill.entity.CourseEnrollment;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findByEmployeeId(Long employeeId);
    List<CourseEnrollment> findByStudentId(Long studentId);
    Optional<CourseEnrollment> findByCourseIdAndEmployeeId(Long courseId, Long employeeId);
    Optional<CourseEnrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);
    long countByStatus(String status);
}
