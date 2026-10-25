package com.skillsphere.skill.service;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.AssessmentDefinition;
import com.skillsphere.skill.entity.AssessmentQuestion;
import com.skillsphere.skill.entity.Skill;
import com.skillsphere.skill.repository.AssessmentDefinitionRepository;
import com.skillsphere.skill.repository.AssessmentQuestionRepository;
import com.skillsphere.skill.repository.LearnerAssessmentAttemptRepository;
import com.skillsphere.skill.repository.SkillRepository;
import com.skillsphere.skill.service.LearningModels.AssessmentRequest;
import com.skillsphere.skill.service.LearningModels.AssessmentView;
import com.skillsphere.skill.service.LearningModels.LearningResourceRequest;
import com.skillsphere.skill.service.LearningModels.LearningResourceView;
import com.skillsphere.skill.service.LearningModels.QuestionRequest;
import com.skillsphere.skill.service.LearningModels.QuestionView;
import com.skillsphere.skill.service.LearningModels.ResourceRecommendation;
import com.skillsphere.skill.entity.LearningResource;
import com.skillsphere.skill.repository.LearningResourceRepository;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@Service
@Transactional
public class QuestionBankService {
    private final AssessmentDefinitionRepository assessmentRepository;
    private final AssessmentQuestionRepository questionRepository;
    private final LearnerAssessmentAttemptRepository attemptRepository;
    private final SkillRepository skillRepository;
    private final LearningResourceRepository resourceRepository;
    private final CompetencyFrameworkService frameworkService;

    public QuestionBankService(AssessmentDefinitionRepository assessmentRepository,
            AssessmentQuestionRepository questionRepository,
            LearnerAssessmentAttemptRepository attemptRepository,
            SkillRepository skillRepository,
            LearningResourceRepository resourceRepository,
            CompetencyFrameworkService frameworkService) {
        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.skillRepository = skillRepository;
        this.resourceRepository = resourceRepository;
        this.frameworkService = frameworkService;
    }

    public AssessmentView createAssessment(AssessmentRequest request) {
        validateAssessment(request);
        AssessmentDefinition assessment = new AssessmentDefinition();
        populateAssessment(assessment, request);
        return assessmentView(assessmentRepository.save(assessment), null);
    }

    public AssessmentView updateAssessment(Long id, AssessmentRequest request) {
        validateAssessment(request);
        AssessmentDefinition assessment = getAssessmentForUpdate(id);
        boolean activeOnlyChange = request.title().equals(assessment.getTitle())
                && request.instructions().equals(assessment.getInstructions())
                && request.skillId().equals(assessment.getSkill().getId())
                && request.passPercentage() == assessment.getPassPercentage()
                && request.maxAttempts() == assessment.getMaxAttempts();
        if (!activeOnlyChange && hasAttempts(id)) {
            throw new IllegalStateException("Assessment content cannot change after a learner has attempted it");
        }
        populateAssessment(assessment, request);
        return assessmentView(assessmentRepository.save(assessment), null);
    }

    public List<AssessmentView> getAdminAssessments() {
        return assessmentRepository.findAll().stream()
                .map(assessment -> assessmentView(assessment, null)).toList();
    }

    public AssessmentView addQuestion(Long assessmentId, QuestionRequest request) {
        validateQuestion(request);
        AssessmentDefinition assessment = getAssessmentForUpdate(assessmentId);
        if (hasAttempts(assessmentId)) {
            throw new IllegalStateException("Questions cannot change after a learner has attempted this assessment");
        }
        if (questionRepository.findByAssessment_IdOrderById(assessmentId).size() >= 100) {
            throw new IllegalStateException("An assessment cannot contain more than 100 questions");
        }
        AssessmentQuestion question = new AssessmentQuestion();
        question.setAssessment(assessment);
        question.setPrompt(request.prompt().trim());
        question.setOptions(request.options().stream().map(String::trim).toList());
        question.setCorrectOptionIndex(request.correctOptionIndex());
        question.setDifficulty(request.difficulty());
        question.setPoints(request.points() == null ? 1 : request.points());
        questionRepository.save(question);
        return assessmentView(assessment, null);
    }

    public void deleteQuestion(Long assessmentId, Long questionId) {
        AssessmentDefinition assessment = getAssessmentForUpdate(assessmentId);
        AssessmentQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + questionId));
        if (!assessmentId.equals(assessment.getId()) || !assessmentId.equals(question.getAssessment().getId())) {
            throw new ResourceNotFoundException("Question not found in assessment: " + assessmentId);
        }
        if (hasAttempts(assessmentId)) {
            throw new IllegalStateException("Questions cannot change after a learner has attempted this assessment");
        }
        questionRepository.delete(question);
    }

    public List<QuestionView> getAdminQuestions(Long assessmentId) {
        getAssessment(assessmentId);
        return questionRepository.findByAssessment_IdOrderById(assessmentId).stream()
                .map(this::questionView).toList();
    }

    public List<AssessmentDefinition> getActiveAssessments() {
        return assessmentRepository.findByActiveTrueOrderByTitleAsc();
    }

    public AssessmentDefinition getAssessment(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + id));
    }

    public AssessmentDefinition getAssessmentForUpdate(Long id) {
        return assessmentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ResourceRecommendation> getRecommendations(Long frameworkId, Long studentId) {
        List<com.skillsphere.skill.entity.CompetencyGap> gaps =
                frameworkService.getStudentGapReport(frameworkId, studentId);
        List<Long> gapSkillIds = gaps.stream().filter(gap -> gap.getGap() > 0)
                .map(com.skillsphere.skill.entity.CompetencyGap::getSkillId).toList();
        if (gapSkillIds.isEmpty()) {
            return List.of();
        }
        return resourceRepository.findByActiveTrueAndSkill_IdInOrderBySkill_NameAscTitleAsc(gapSkillIds)
                .stream()
                .map(resource -> {
                    com.skillsphere.skill.entity.CompetencyGap gap = gaps.stream()
                            .filter(item -> item.getSkillId().equals(resource.getSkill().getId()))
                            .findFirst().orElseThrow();
                    return new ResourceRecommendation(resourceView(resource), gap.getGap(),
                            gap.getEmployeeProficiency() == null ? 0 : gap.getEmployeeProficiency(),
                            gap.getRequiredProficiency());
                })
                .sorted((left, right) -> Integer.compare(right.priorityGap(), left.priorityGap()))
                .toList();
    }

    public List<LearningResourceView> getResources() {
        return resourceRepository.findAll().stream().map(this::resourceView).toList();
    }

    public LearningResourceView createResource(LearningResourceRequest request) {
        validateResource(request);
        LearningResource resource = new LearningResource();
        populateResource(resource, request);
        return resourceView(resourceRepository.save(resource));
    }

    public LearningResourceView updateResource(Long id, LearningResourceRequest request) {
        validateResource(request);
        LearningResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning resource not found: " + id));
        populateResource(resource, request);
        return resourceView(resourceRepository.save(resource));
    }

    private void populateAssessment(AssessmentDefinition assessment, AssessmentRequest request) {
        Skill skill = skillRepository.findById(request.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + request.skillId()));
        assessment.setTitle(request.title().trim());
        assessment.setInstructions(request.instructions().trim());
        assessment.setSkill(skill);
        assessment.setPassPercentage(request.passPercentage());
        assessment.setMaxAttempts(request.maxAttempts());
        assessment.setActive(request.active() == null || request.active());
    }

    private void validateAssessment(AssessmentRequest request) {
        if (request == null || request.title() == null || request.title().isBlank()
                || request.instructions() == null || request.instructions().isBlank()
                || request.skillId() == null || request.skillId() <= 0
                || request.passPercentage() == null || request.passPercentage() < 1
                || request.passPercentage() > 100 || request.maxAttempts() == null
                || request.maxAttempts() < 1 || request.maxAttempts() > 10) {
            throw new IllegalArgumentException("Assessment title, instructions, skill, pass threshold (1-100), and attempt limit (1-10) are required");
        }
    }

    private void validateQuestion(QuestionRequest request) {
        if (request == null || request.prompt() == null || request.prompt().isBlank()
                || request.options() == null || request.options().size() < 2
                || request.options().size() > 6 || request.options().stream().anyMatch(value -> value == null || value.isBlank())
                || request.correctOptionIndex() == null || request.correctOptionIndex() < 0
                || request.correctOptionIndex() >= request.options().size()
                || request.difficulty() == null
                || (request.points() != null && (request.points() < 1 || request.points() > 10))) {
            throw new IllegalArgumentException("Question requires a prompt, 2-6 options, valid correct option, difficulty, and 1-10 points");
        }
    }

    private boolean hasAttempts(Long assessmentId) {
        return attemptRepository.existsByAssessment_Id(assessmentId);
    }

    private AssessmentView assessmentView(AssessmentDefinition assessment, Integer attemptsRemaining) {
        int questionCount = questionRepository.findByAssessment_IdOrderById(assessment.getId()).size();
        return new AssessmentView(assessment.getId(), assessment.getTitle(), assessment.getInstructions(),
                assessment.getSkill().getId(), assessment.getSkill().getName(), assessment.getPassPercentage(),
                assessment.getMaxAttempts(), assessment.isActive(), questionCount, attemptsRemaining);
    }

    private QuestionView questionView(AssessmentQuestion question) {
        return new QuestionView(question.getId(), question.getPrompt(), question.getOptions(),
                question.getDifficulty(), question.getPoints());
    }

    private void validateResource(LearningResourceRequest request) {
        if (request == null || request.title() == null || request.title().isBlank()
                || request.description() == null || request.description().isBlank()
                || request.skillId() == null || request.resourceType() == null || request.resourceType().isBlank()
                || request.difficultyLevel() == null || request.difficultyLevel().isBlank()) {
            throw new IllegalArgumentException("Title, description, skill, resource type, and difficulty are required");
        }
        if (request.verifiedUrl() != null && !request.verifiedUrl().isBlank()) {
            try {
                URI uri = URI.create(request.verifiedUrl().trim());
                if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                        || uri.getHost() == null) {
                    throw new IllegalArgumentException("Resource URL must be an absolute HTTP or HTTPS URL");
                }
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Resource URL must be an absolute HTTP or HTTPS URL", exception);
            }
        }
    }

    private void populateResource(LearningResource resource, LearningResourceRequest request) {
        Skill skill = skillRepository.findById(request.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + request.skillId()));
        resource.setTitle(request.title().trim());
        resource.setDescription(request.description().trim());
        resource.setSkill(skill);
        resource.setResourceType(request.resourceType().trim());
        resource.setDifficultyLevel(request.difficultyLevel().trim());
        resource.setVerifiedUrl(request.verifiedUrl() == null || request.verifiedUrl().isBlank()
                ? null : request.verifiedUrl().trim());
        resource.setActive(request.active() == null || request.active());
    }

    private LearningResourceView resourceView(LearningResource resource) {
        return new LearningResourceView(resource.getId(), resource.getTitle(), resource.getDescription(),
                resource.getSkill().getId(), resource.getSkill().getName(), resource.getResourceType(),
                resource.getDifficultyLevel(), resource.getVerifiedUrl(), resource.isActive());
    }
}
