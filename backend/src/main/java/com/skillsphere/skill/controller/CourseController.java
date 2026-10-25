package com.skillsphere.skill.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.skillsphere.skill.entity.Course;
import com.skillsphere.skill.entity.CourseEnrollment;
import com.skillsphere.skill.entity.LearningPath;
import com.skillsphere.skill.security.AuthorizationService;
import com.skillsphere.skill.service.CourseService;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final AuthorizationService authorizationService;

    public CourseController(CourseService courseService, AuthorizationService authorizationService) {
        this.courseService = courseService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Course createCourse(@RequestBody Course course) {
        return courseService.createCourse(course);
    }

    @GetMapping("/learning-paths")
    public List<LearningPath> getAllLearningPaths() {
        return courseService.getAllLearningPaths();
    }

    @PostMapping("/learning-paths")
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public LearningPath createLearningPath(@RequestBody LearningPath path) {
        return courseService.createLearningPath(path);
    }

    @GetMapping("/enrollments/me")
    public List<CourseEnrollment> getMyEnrollments() {
        if (authorizationService.isEmployee()) {
            return courseService.getEnrollmentsByEmployee(authorizationService.getCurrentEmployeeId());
        }
        if (authorizationService.isStudent()) {
            return courseService.getEnrollmentsByStudent(authorizationService.getCurrentStudentId());
        }
        if (authorizationService.isHrAdmin()) {
            return courseService.getAdminEnrollments();
        }
        return List.of();
    }

    @PostMapping("/{courseId}/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseEnrollment enrollInCourse(@PathVariable Long courseId) {
        if (authorizationService.isEmployee()) {
            return courseService.enrollEmployee(courseId, authorizationService.getCurrentEmployeeId());
        }
        if (authorizationService.isStudent()) {
            return courseService.enrollStudent(courseId, authorizationService.getCurrentStudentId());
        }
        if (authorizationService.isHrAdmin()) {
            return courseService.enrollAdmin(courseId);
        }
        throw new org.springframework.security.access.AccessDeniedException("Must be an employee, student, or admin to enroll");
    }

    @PutMapping("/enrollments/{enrollmentId}/progress")
    public CourseEnrollment updateProgress(@PathVariable Long enrollmentId, @RequestParam int progress) {
        return courseService.updateProgress(enrollmentId, progress);
    }
}
