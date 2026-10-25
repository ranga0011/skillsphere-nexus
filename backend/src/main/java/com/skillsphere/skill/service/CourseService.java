package com.skillsphere.skill.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.Course;
import com.skillsphere.skill.entity.CourseEnrollment;
import com.skillsphere.skill.entity.Employee;
import com.skillsphere.skill.entity.LearningPath;
import com.skillsphere.skill.entity.Student;
import com.skillsphere.skill.repository.CourseEnrollmentRepository;
import com.skillsphere.skill.repository.CourseRepository;
import com.skillsphere.skill.repository.LearningPathRepository;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.repository.StudentRepository;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LearningPathRepository learningPathRepository;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;

    public CourseService(CourseRepository courseRepository,
                         CourseEnrollmentRepository enrollmentRepository,
                         LearningPathRepository learningPathRepository,
                         EmployeeRepository employeeRepository,
                         StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.learningPathRepository = learningPathRepository;
        this.employeeRepository = employeeRepository;
        this.studentRepository = studentRepository;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }

    @Transactional
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    public List<LearningPath> getAllLearningPaths() {
        return learningPathRepository.findAll();
    }

    @Transactional
    public LearningPath createLearningPath(LearningPath path) {
        return learningPathRepository.save(path);
    }

    public List<CourseEnrollment> getEnrollmentsByEmployee(Long employeeId) {
        return enrollmentRepository.findByEmployeeId(employeeId);
    }

    public List<CourseEnrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    public List<CourseEnrollment> getAdminEnrollments() {
        return enrollmentRepository.findAll();
    }

    @Transactional
    public CourseEnrollment enrollAdmin(Long courseId) {
        Course course = getCourseById(courseId);
        return enrollmentRepository.findAll().stream()
                .filter(e -> e.getCourse() != null && e.getCourse().getId().equals(courseId) && e.getEmployee() == null && e.getStudent() == null)
                .findFirst()
                .orElseGet(() -> {
                    CourseEnrollment enrollment = new CourseEnrollment();
                    enrollment.setCourse(course);
                    enrollment.setStatus("ENROLLED");
                    enrollment.setProgressPercentage(0);
                    enrollment.setEnrolledAt(LocalDate.now());

                    course.setTotalEnrolled(course.getTotalEnrolled() + 1);
                    courseRepository.save(course);

                    return enrollmentRepository.save(enrollment);
                });
    }

    @Transactional
    public CourseEnrollment enrollEmployee(Long courseId, Long employeeId) {
        Course course = getCourseById(courseId);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));

        return enrollmentRepository.findByCourseIdAndEmployeeId(courseId, employeeId)
                .orElseGet(() -> {
                    CourseEnrollment enrollment = new CourseEnrollment();
                    enrollment.setCourse(course);
                    enrollment.setEmployee(employee);
                    enrollment.setStatus("ENROLLED");
                    enrollment.setProgressPercentage(0);
                    enrollment.setEnrolledAt(LocalDate.now());

                    course.setTotalEnrolled(course.getTotalEnrolled() + 1);
                    courseRepository.save(course);

                    return enrollmentRepository.save(enrollment);
                });
    }

    @Transactional
    public CourseEnrollment enrollStudent(Long courseId, Long studentId) {
        Course course = getCourseById(courseId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        return enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId)
                .orElseGet(() -> {
                    CourseEnrollment enrollment = new CourseEnrollment();
                    enrollment.setCourse(course);
                    enrollment.setStudent(student);
                    enrollment.setStatus("ENROLLED");
                    enrollment.setProgressPercentage(0);
                    enrollment.setEnrolledAt(LocalDate.now());

                    course.setTotalEnrolled(course.getTotalEnrolled() + 1);
                    courseRepository.save(course);

                    return enrollmentRepository.save(enrollment);
                });
    }

    @Transactional
    public CourseEnrollment updateProgress(Long enrollmentId, int progressPercentage) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));

        int clamped = Math.min(100, Math.max(0, progressPercentage));
        enrollment.setProgressPercentage(clamped);

        if (clamped >= 100) {
            enrollment.setStatus("COMPLETED");
            if (enrollment.getCompletedAt() == null) {
                enrollment.setCompletedAt(LocalDate.now());
                Course course = enrollment.getCourse();
                course.setTotalCompleted(course.getTotalCompleted() + 1);
                courseRepository.save(course);
            }
        } else if (clamped > 0) {
            enrollment.setStatus("IN_PROGRESS");
        }

        return enrollmentRepository.save(enrollment);
    }
}
