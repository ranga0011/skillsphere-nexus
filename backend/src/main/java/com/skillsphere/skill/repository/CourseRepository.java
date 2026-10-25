package com.skillsphere.skill.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.skillsphere.skill.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCourseTypeIgnoreCase(String courseType);
    List<Course> findByCategoryIgnoreCase(String category);
}
