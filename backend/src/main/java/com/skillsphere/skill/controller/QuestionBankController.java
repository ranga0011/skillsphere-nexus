package com.skillsphere.skill.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.skillsphere.skill.service.LearningModels.AssessmentRequest;
import com.skillsphere.skill.service.LearningModels.AssessmentView;
import com.skillsphere.skill.service.LearningModels.LearningResourceRequest;
import com.skillsphere.skill.service.LearningModels.LearningResourceView;
import com.skillsphere.skill.service.LearningModels.QuestionRequest;
import com.skillsphere.skill.service.LearningModels.QuestionView;
import com.skillsphere.skill.service.QuestionBankService;

@RestController
@RequestMapping("/api/admin/learning")
@PreAuthorize("hasRole('HR_ADMIN')")
public class QuestionBankController {
    private final QuestionBankService questionBankService;

    public QuestionBankController(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    @GetMapping("/assessments")
    public List<AssessmentView> getAssessments() {
        return questionBankService.getAdminAssessments();
    }

    @PostMapping("/assessments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentView createAssessment(@RequestBody AssessmentRequest request) {
        return questionBankService.createAssessment(request);
    }

    @PutMapping("/assessments/{id}")
    public AssessmentView updateAssessment(@PathVariable Long id, @RequestBody AssessmentRequest request) {
        return questionBankService.updateAssessment(id, request);
    }

    @GetMapping("/assessments/{id}/questions")
    public List<QuestionView> getQuestions(@PathVariable Long id) {
        return questionBankService.getAdminQuestions(id);
    }

    @PostMapping("/assessments/{id}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentView addQuestion(@PathVariable Long id, @RequestBody QuestionRequest request) {
        return questionBankService.addQuestion(id, request);
    }

    @DeleteMapping("/assessments/{id}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(@PathVariable Long id, @PathVariable Long questionId) {
        questionBankService.deleteQuestion(id, questionId);
    }

    @GetMapping("/resources")
    public List<LearningResourceView> getResources() {
        return questionBankService.getResources();
    }

    @PostMapping("/resources")
    @ResponseStatus(HttpStatus.CREATED)
    public LearningResourceView createResource(@RequestBody LearningResourceRequest request) {
        return questionBankService.createResource(request);
    }

    @PutMapping("/resources/{id}")
    public LearningResourceView updateResource(@PathVariable Long id, @RequestBody LearningResourceRequest request) {
        return questionBankService.updateResource(id, request);
    }
}
