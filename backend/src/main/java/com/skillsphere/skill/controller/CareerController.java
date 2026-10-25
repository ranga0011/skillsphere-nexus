package com.skillsphere.skill.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.skillsphere.skill.entity.CareerPlan;
import com.skillsphere.skill.entity.InternalJob;
import com.skillsphere.skill.entity.JobApplication;
import com.skillsphere.skill.security.AuthorizationService;
import com.skillsphere.skill.service.CareerService;

@RestController
@RequestMapping("/api/career")
public class CareerController {

    private final CareerService careerService;
    private final AuthorizationService authorizationService;

    public CareerController(CareerService careerService, AuthorizationService authorizationService) {
        this.careerService = careerService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/plan/me")
    public CareerPlan getMyCareerPlan() {
        if (authorizationService.isEmployee()) {
            List<CareerPlan> plans = careerService.getPlansByEmployee(authorizationService.getCurrentEmployeeId());
            return plans.isEmpty() ? null : plans.get(0);
        }
        if (authorizationService.isStudent()) {
            List<CareerPlan> plans = careerService.getPlansByStudent(authorizationService.getCurrentStudentId());
            return plans.isEmpty() ? null : plans.get(0);
        }
        return null;
    }

    @PostMapping("/plan/me")
    public CareerPlan saveMyCareerPlan(@RequestBody CareerPlan plan) {
        Long empId = authorizationService.isEmployee() ? authorizationService.getCurrentEmployeeId() : null;
        Long studentId = authorizationService.isStudent() ? authorizationService.getCurrentStudentId() : null;
        return careerService.createOrUpdatePlan(plan, empId, studentId);
    }

    @GetMapping("/jobs")
    public List<InternalJob> getAllJobs() {
        return careerService.getAllJobs();
    }

    @PostMapping("/jobs")
    @PreAuthorize("hasRole('HR_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public InternalJob createJob(@RequestBody InternalJob job) {
        return careerService.createJob(job);
    }

    @GetMapping("/applications/me")
    public List<JobApplication> getMyApplications() {
        if (authorizationService.isEmployee()) {
            return careerService.getApplicationsByEmployee(authorizationService.getCurrentEmployeeId());
        }
        if (authorizationService.isStudent()) {
            return careerService.getApplicationsByStudent(authorizationService.getCurrentStudentId());
        }
        return List.of();
    }

    @PostMapping("/jobs/{jobId}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplication applyForJob(@PathVariable Long jobId) {
        Long empId = authorizationService.isEmployee() ? authorizationService.getCurrentEmployeeId() : null;
        Long studentId = authorizationService.isStudent() ? authorizationService.getCurrentStudentId() : null;
        return careerService.applyForJob(jobId, empId, studentId);
    }
}
