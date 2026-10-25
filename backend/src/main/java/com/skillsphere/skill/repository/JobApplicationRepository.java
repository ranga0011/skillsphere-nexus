package com.skillsphere.skill.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.skillsphere.skill.entity.JobApplication;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByEmployeeId(Long employeeId);
    List<JobApplication> findByStudentId(Long studentId);
    Optional<JobApplication> findByJobIdAndEmployeeId(Long jobId, Long employeeId);
    Optional<JobApplication> findByJobIdAndStudentId(Long jobId, Long studentId);
}
