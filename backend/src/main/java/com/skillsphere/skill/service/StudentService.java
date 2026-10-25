package com.skillsphere.skill.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.Student;
import com.skillsphere.skill.repository.StudentRepository;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    public Student updateStudent(Long id, String name, String email, String program) {
        Student student = getStudentById(id);
        if (name == null || name.isBlank() || email == null || email.isBlank()
                || program == null || program.isBlank()) {
            throw new IllegalArgumentException("Name, email, and program are required");
        }
        if (studentRepository.existsByEmailAndIdNot(email.trim().toLowerCase(java.util.Locale.ROOT), id)) {
            throw new IllegalArgumentException("Student email is already registered");
        }
        student.setName(name.trim());
        student.setEmail(email.trim().toLowerCase(java.util.Locale.ROOT));
        student.setProgram(program.trim());
        return studentRepository.save(student);
    }
}
