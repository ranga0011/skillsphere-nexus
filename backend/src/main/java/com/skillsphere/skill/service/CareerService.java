package com.skillsphere.skill.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.CareerPlan;
import com.skillsphere.skill.entity.Employee;
import com.skillsphere.skill.entity.InternalJob;
import com.skillsphere.skill.entity.JobApplication;
import com.skillsphere.skill.entity.Student;
import com.skillsphere.skill.repository.CareerPlanRepository;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.repository.InternalJobRepository;
import com.skillsphere.skill.repository.JobApplicationRepository;
import com.skillsphere.skill.repository.StudentRepository;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@Service
public class CareerService {

    private final CareerPlanRepository careerPlanRepository;
    private final InternalJobRepository internalJobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;

    public CareerService(CareerPlanRepository careerPlanRepository,
                         InternalJobRepository internalJobRepository,
                         JobApplicationRepository jobApplicationRepository,
                         EmployeeRepository employeeRepository,
                         StudentRepository studentRepository) {
        this.careerPlanRepository = careerPlanRepository;
        this.internalJobRepository = internalJobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.employeeRepository = employeeRepository;
        this.studentRepository = studentRepository;
    }

    public List<CareerPlan> getPlansByEmployee(Long employeeId) {
        return careerPlanRepository.findByEmployeeId(employeeId);
    }

    public List<CareerPlan> getPlansByStudent(Long studentId) {
        return careerPlanRepository.findByStudentId(studentId);
    }

    @Transactional
    public CareerPlan createOrUpdatePlan(CareerPlan plan, Long employeeId, Long studentId) {
        if (employeeId != null) {
            Employee emp = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
            plan.setEmployee(emp);
        } else if (studentId != null) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
            plan.setStudent(student);
        }
        return careerPlanRepository.save(plan);
    }

    public List<InternalJob> getAllJobs() {
        return internalJobRepository.findAll();
    }

    @Transactional
    public InternalJob createJob(InternalJob job) {
        return internalJobRepository.save(job);
    }

    public List<JobApplication> getApplicationsByEmployee(Long employeeId) {
        return jobApplicationRepository.findByEmployeeId(employeeId);
    }

    public List<JobApplication> getApplicationsByStudent(Long studentId) {
        return jobApplicationRepository.findByStudentId(studentId);
    }

    @Transactional
    public JobApplication applyForJob(Long jobId, Long employeeId, Long studentId) {
        InternalJob job = internalJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        JobApplication app = new JobApplication();
        app.setJob(job);
        app.setAppliedAt(LocalDate.now());
        app.setStatus("APPLIED");
        app.setMatchPercentage(85); // Evaluated match score

        if (employeeId != null) {
            Employee emp = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
            app.setEmployee(emp);
            return jobApplicationRepository.findByJobIdAndEmployeeId(jobId, employeeId)
                    .orElseGet(() -> jobApplicationRepository.save(app));
        } else if (studentId != null) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
            app.setStudent(student);
            return jobApplicationRepository.findByJobIdAndStudentId(jobId, studentId)
                    .orElseGet(() -> jobApplicationRepository.save(app));
        }
        return jobApplicationRepository.save(app);
    }
}
