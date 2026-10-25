package com.skillsphere.skill.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.AssessmentDefinition;
import com.skillsphere.skill.entity.AssessmentQuestion;
import com.skillsphere.skill.entity.Employee;
import com.skillsphere.skill.entity.LearnerAssessmentAttempt;
import com.skillsphere.skill.entity.LearnerAttemptAnswer;
import com.skillsphere.skill.entity.LearnerAttemptStatus;
import com.skillsphere.skill.entity.SkillAssessment;
import com.skillsphere.skill.entity.Student;
import com.skillsphere.skill.entity.VerificationStatus;
import com.skillsphere.skill.repository.AssessmentQuestionRepository;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.repository.LearnerAssessmentAttemptRepository;
import com.skillsphere.skill.repository.LearnerAttemptAnswerRepository;
import com.skillsphere.skill.repository.StudentRepository;
import com.skillsphere.skill.service.CertificateModels.IssuedCertificateView;
import com.skillsphere.skill.service.LearningModels.AssessmentView;
import com.skillsphere.skill.service.LearningModels.AttemptResult;
import com.skillsphere.skill.service.LearningModels.QuestionView;
import com.skillsphere.skill.service.LearningModels.StartedAttempt;
import com.skillsphere.skill.service.LearningModels.SubmitAttemptRequest;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@Service
@Transactional
public class LearnerAssessmentService {
    private final QuestionBankService questionBankService;
    private final AssessmentQuestionRepository questionRepository;
    private final LearnerAssessmentAttemptRepository attemptRepository;
    private final LearnerAttemptAnswerRepository answerRepository;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;
    private final SkillAssessmentService skillAssessmentService;
    private final IssuedCertificateService certificateService;

    public LearnerAssessmentService(QuestionBankService questionBankService,
            AssessmentQuestionRepository questionRepository,
            LearnerAssessmentAttemptRepository attemptRepository,
            LearnerAttemptAnswerRepository answerRepository,
            EmployeeRepository employeeRepository,
            StudentRepository studentRepository,
            SkillAssessmentService skillAssessmentService,
            IssuedCertificateService certificateService) {
        this.questionBankService = questionBankService;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.employeeRepository = employeeRepository;
        this.studentRepository = studentRepository;
        this.skillAssessmentService = skillAssessmentService;
        this.certificateService = certificateService;
    }

    @Transactional(readOnly = true)
    public List<AssessmentView> listAvailable(Long employeeId, Long studentId) {
        requireOneOwner(employeeId, studentId);
        return questionBankService.getActiveAssessments().stream()
                .filter(assessment -> questionRepository.findByAssessment_IdOrderById(assessment.getId()).size() > 0)
                .map(assessment -> {
                    long used = attemptCount(assessment.getId(), employeeId, studentId);
                    int remaining = (int) Math.max(0, assessment.getMaxAttempts() - used);
                    return new AssessmentView(assessment.getId(), assessment.getTitle(), assessment.getInstructions(),
                            assessment.getSkill().getId(), assessment.getSkill().getName(),
                            assessment.getPassPercentage(), assessment.getMaxAttempts(),
                            assessment.isActive(),
                            questionRepository.findByAssessment_IdOrderById(assessment.getId()).size(), remaining);
                }).filter(assessment -> assessment.attemptsRemaining() > 0).toList();
    }

    public StartedAttempt start(Long assessmentId, Long employeeId, Long studentId) {
        requireOneOwner(employeeId, studentId);
        AssessmentDefinition definition = questionBankService.getAssessmentForUpdate(assessmentId);
        if (!definition.isActive()) {
            throw new IllegalStateException("This assessment is not currently active");
        }
        List<AssessmentQuestion> questions = questionRepository.findByAssessment_IdOrderById(assessmentId);
        if (questions.isEmpty()) {
            throw new IllegalStateException("This assessment has no questions yet");
        }
        long attemptsUsed = attemptCount(assessmentId, employeeId, studentId);
        if (attemptsUsed >= definition.getMaxAttempts()) {
            throw new IllegalStateException("The maximum number of attempts has been reached");
        }

        LearnerAssessmentAttempt attempt = new LearnerAssessmentAttempt();
        attempt.setAssessment(definition);
        attempt.setAttemptNumber(Math.toIntExact(attemptsUsed + 1));
        attempt.setStartedAt(Instant.now());
        if (studentId != null) {
            attempt.setStudent(studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId)));
        } else {
            attempt.setEmployee(employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId)));
        }
        LearnerAssessmentAttempt saved = attemptRepository.save(attempt);
        AssessmentView assessmentView = new AssessmentView(definition.getId(), definition.getTitle(),
                definition.getInstructions(), definition.getSkill().getId(), definition.getSkill().getName(),
                definition.getPassPercentage(), definition.getMaxAttempts(), definition.isActive(),
                questions.size(), (int) (definition.getMaxAttempts() - attemptsUsed - 1));
        List<QuestionView> questionViews = questions.stream()
                .map(question -> new QuestionView(question.getId(), question.getPrompt(),
                        question.getOptions(), question.getDifficulty(), question.getPoints()))
                .toList();
        return new StartedAttempt(saved.getId(), saved.getAttemptNumber(), saved.getStartedAt(),
                assessmentView, questionViews);
    }

    public AttemptResult submit(Long attemptId, Long employeeId, Long studentId,
            SubmitAttemptRequest request) {
        requireOneOwner(employeeId, studentId);
        LearnerAssessmentAttempt attempt = attemptRepository.findByIdForUpdate(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment attempt not found: " + attemptId));
        requireOwner(attempt, employeeId, studentId);
        if (attempt.getStatus() != LearnerAttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("This attempt has already been submitted");
        }
        List<AssessmentQuestion> questions =
                questionRepository.findByAssessment_IdOrderById(attempt.getAssessment().getId());
        if (request == null || request.answers() == null || request.answers().size() != questions.size()) {
            throw new IllegalArgumentException("Submit exactly one answer for every assessment question");
        }
        Map<Long, Integer> submitted = new HashMap<>();
        for (var answer : request.answers()) {
            if (answer == null || answer.questionId() == null || answer.selectedOptionIndex() == null
                    || submitted.putIfAbsent(answer.questionId(), answer.selectedOptionIndex()) != null) {
                throw new IllegalArgumentException("Answers must contain unique question ids and selected options");
            }
        }
        if (submitted.size() != questions.size()
                || questions.stream().anyMatch(question -> !submitted.containsKey(question.getId()))) {
            throw new IllegalArgumentException("Submitted answers do not match this assessment");
        }
        int earnedPoints = 0;
        int maximumPoints = 0;
        for (AssessmentQuestion question : questions) {
            int selectedIndex = submitted.get(question.getId());
            if (selectedIndex < 0 || selectedIndex >= question.getOptions().size()) {
                throw new IllegalArgumentException("A selected option is invalid");
            }
            maximumPoints += question.getPoints();
            if (selectedIndex == question.getCorrectOptionIndex()) {
                earnedPoints += question.getPoints();
            }
            answerRepository.save(new LearnerAttemptAnswer(attempt, question, selectedIndex));
        }
        int percentage = (int) Math.floor(earnedPoints * 100.0 / maximumPoints);
        boolean passed = earnedPoints * 100.0 / maximumPoints >= attempt.getAssessment().getPassPercentage();
        attempt.setScorePoints(earnedPoints);
        attempt.setMaxPoints(maximumPoints);
        attempt.setPercentage(percentage);
        attempt.setPassed(passed);
        attempt.setSubmittedAt(Instant.now());
        attempt.setStatus(LearnerAttemptStatus.SUBMITTED);
        attemptRepository.save(attempt);

        IssuedCertificateView certificate = null;
        if (passed) {
            SkillAssessment recorded = studentId != null
                    ? skillAssessmentService.createStudentAssessment(studentId,
                            attempt.getAssessment().getSkill().getId(), earnedPoints, maximumPoints,
                            "ONLINE_MULTIPLE_CHOICE", "Server-scored assessment", LocalDate.now())
                    : skillAssessmentService.createAssessment(employeeId,
                            attempt.getAssessment().getSkill().getId(), earnedPoints, maximumPoints,
                            "ONLINE_MULTIPLE_CHOICE", "Server-scored assessment", LocalDate.now());
            attempt.setSkillAssessment(recorded);
            attemptRepository.save(attempt);
            skillAssessmentService.verifyAssessment(recorded.getId(), VerificationStatus.VERIFIED, "assessment-engine");
            certificate = certificateService.getForAssessment(recorded.getId()).orElse(null);
        }
        return result(attempt, certificate);
    }

    @Transactional(readOnly = true)
    public List<AttemptResult> history(Long employeeId, Long studentId) {
        requireOneOwner(employeeId, studentId);
        List<LearnerAssessmentAttempt> attempts = studentId != null
                ? attemptRepository.findByStudent_IdOrderByStartedAtDesc(studentId)
                : attemptRepository.findByEmployee_IdOrderByStartedAtDesc(employeeId);
        return attempts.stream().filter(attempt -> attempt.getStatus() == LearnerAttemptStatus.SUBMITTED)
                .map(attempt -> result(attempt, attempt.getSkillAssessment() == null
                        ? null
                        : certificateService.getForAssessment(attempt.getSkillAssessment().getId()).orElse(null)))
                .toList();
    }

    private AttemptResult result(LearnerAssessmentAttempt attempt, IssuedCertificateView certificate) {
        return new AttemptResult(attempt.getId(), attempt.getAssessment().getId(),
                attempt.getAssessment().getTitle(), attempt.getStatus(), attempt.getStartedAt(),
                attempt.getSubmittedAt(), attempt.getAttemptNumber(), attempt.getScorePoints() == null
                        ? 0 : attempt.getScorePoints(),
                attempt.getMaxPoints() == null ? 0 : attempt.getMaxPoints(),
                attempt.getPercentage() == null ? 0 : attempt.getPercentage(), attempt.isPassed(),
                attempt.getAssessment().getPassPercentage(), certificate);
    }

    private long attemptCount(Long assessmentId, Long employeeId, Long studentId) {
        return studentId != null
                ? attemptRepository.countByAssessment_IdAndStudent_Id(assessmentId, studentId)
                : attemptRepository.countByAssessment_IdAndEmployee_Id(assessmentId, employeeId);
    }

    private void requireOneOwner(Long employeeId, Long studentId) {
        if ((employeeId == null) == (studentId == null)) {
            throw new IllegalArgumentException("Exactly one linked learner profile is required");
        }
    }

    private void requireOwner(LearnerAssessmentAttempt attempt, Long employeeId, Long studentId) {
        boolean owns = studentId != null
                ? attempt.getStudent() != null && studentId.equals(attempt.getStudent().getId())
                : attempt.getEmployee() != null && employeeId.equals(attempt.getEmployee().getId());
        if (!owns) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "This assessment attempt belongs to another learner");
        }
    }
}
