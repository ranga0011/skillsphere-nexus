package com.skillsphere.skill.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.skillsphere.skill.entity.AssessmentDifficulty;
import com.skillsphere.skill.entity.Course;
import com.skillsphere.skill.repository.CourseRepository;
import com.skillsphere.skill.service.LearningModels.QuestionRequest;
import com.skillsphere.skill.service.exception.ResourceNotFoundException;

@Service
public class AiQuestionGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(AiQuestionGeneratorService.class);

    private final CourseRepository courseRepository;

    public AiQuestionGeneratorService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<QuestionRequest> generateQuestionsForCourse(Long courseId, Integer count) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        int targetCount = (count == null || count <= 0) ? 5 : Math.min(count, 20);
        logger.info("Generating {} AI assessment questions for course: '{}' (Category: '{}')",
                targetCount, course.getTitle(), course.getCategory());

        String text = (course.getTitle() + " " + course.getDescription() + " " + course.getCategory()).toLowerCase(Locale.ROOT);

        List<QuestionRequest> candidatePool = new ArrayList<>();

        if (text.contains("spring") || text.contains("java") || text.contains("backend") || text.contains("microservice")) {
            candidatePool.addAll(getJavaSpringBootQuestions());
        }
        if (text.contains("cloud") || text.contains("devops") || text.contains("docker")
                || text.contains("kubernetes") || text.contains("aws") || text.contains("k8s") || text.contains("ci/cd")) {
            candidatePool.addAll(getCloudDevOpsQuestions());
        }
        if (text.contains("angular") || text.contains("frontend") || text.contains("typescript")
                || text.contains("javascript") || text.contains("web") || text.contains("ui")) {
            candidatePool.addAll(getAngularFrontendQuestions());
        }
        if (text.contains("bank") || text.contains("financial") || text.contains("payment")
                || text.contains("compliance") || text.contains("domain")) {
            candidatePool.addAll(getBankingFintechQuestions());
        }
        if (text.contains("python") || text.contains("data") || text.contains("ai")
                || text.contains("machine learning") || text.contains("kafka") || text.contains("spark")) {
            candidatePool.addAll(getDataAiQuestions());
        }

        // Add general distributed systems and system architecture questions as foundation
        candidatePool.addAll(getGeneralArchitectureQuestions());

        Collections.shuffle(candidatePool);

        List<QuestionRequest> selected = new ArrayList<>();
        int selectedCount = Math.min(targetCount, candidatePool.size());
        for (int i = 0; i < selectedCount; i++) {
            selected.add(candidatePool.get(i));
        }

        // If target count still needs more questions, synthesize contextual questions
        int index = selected.size() + 1;
        while (selected.size() < targetCount) {
            selected.add(createSynthesizedQuestion(course, index++));
        }

        logger.info("Successfully generated {} AI questions for course '{}'", selected.size(), course.getTitle());
        return selected;
    }

    private List<QuestionRequest> getJavaSpringBootQuestions() {
        List<QuestionRequest> list = new ArrayList<>();
        list.add(new QuestionRequest(
                "In Spring Boot, which annotation is recommended on configuration classes to declare beans and enable component scanning?",
                List.of("@SpringBootApplication", "@EnableAutoConfiguration", "@Component", "@Service"),
                0, AssessmentDifficulty.BEGINNER, 1));

        list.add(new QuestionRequest(
                "Why is constructor-based dependency injection preferred over field injection (@Autowired on fields)?",
                List.of("It enforces immutability, facilitates unit testing without reflection, and avoids circular dependencies",
                        "Field injection is deprecated in Java 17",
                        "Constructor injection uses less memory in the JVM",
                        "Constructor injection only works with singleton beans"),
                0, AssessmentDifficulty.INTERMEDIATE, 2));

        list.add(new QuestionRequest(
                "Which Spring Data JPA repository method suffix automatically creates an SQL query matching a case-insensitive name search?",
                List.of("findByNameIgnoreCase", "findByNameLikeUpper", "findByNameInsensitive", "queryByNameCaseInsensitive"),
                0, AssessmentDifficulty.INTERMEDIATE, 1));

        list.add(new QuestionRequest(
                "In Spring Security 6 / Spring Boot 3+, how should stateless REST API authentication be configured in SecurityFilterChain?",
                List.of("sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))",
                        "http.disableSessions()",
                        "http.statelessMode(true)",
                        "security.jwt.enabled(true)"),
                0, AssessmentDifficulty.ADVANCED, 2));

        list.add(new QuestionRequest(
                "What is the default isolation level for Spring @Transactional when running on PostgreSQL?",
                List.of("READ_COMMITTED", "READ_UNCOMMITTED", "REPEATABLE_READ", "SERIALIZABLE"),
                0, AssessmentDifficulty.INTERMEDIATE, 1));

        list.add(new QuestionRequest(
                "Which Spring Boot Actuator endpoint exposes system health, readiness, and liveness probes for container environments?",
                List.of("/actuator/health", "/actuator/info", "/actuator/metrics", "/actuator/status"),
                0, AssessmentDifficulty.BEGINNER, 1));

        list.add(new QuestionRequest(
                "What is the primary benefit of using Virtual Threads (Project Loom) in modern Spring Boot 3.2+ applications?",
                List.of("Significantly higher throughput for I/O-bound tasks with lightweight thread concurrency",
                        "Faster CPU matrix calculations",
                        "Automatic garbage collection bypass",
                        "Eliminating the need for Spring Data repositories"),
                0, AssessmentDifficulty.ADVANCED, 2));

        return list;
    }

    private List<QuestionRequest> getCloudDevOpsQuestions() {
        List<QuestionRequest> list = new ArrayList<>();
        list.add(new QuestionRequest(
                "In Kubernetes architecture, what is the smallest deployable computing unit that can be created and managed?",
                List.of("Pod", "Node", "Container", "Cluster"),
                0, AssessmentDifficulty.BEGINNER, 1));

        list.add(new QuestionRequest(
                "In a multi-stage Dockerfile, what is the primary architectural purpose of separating the build stage from the runtime stage?",
                List.of("To minimize the final container image size and reduce security attack surface",
                        "To compile code in parallel across CPU cores",
                        "To allow root access during build only",
                        "To bypass Docker image registry rate limits"),
                0, AssessmentDifficulty.INTERMEDIATE, 2));

        list.add(new QuestionRequest(
                "Which Kubernetes Service type creates a cloud provider load balancer with an external public IP address?",
                List.of("LoadBalancer", "ClusterIP", "NodePort", "ExternalName"),
                0, AssessmentDifficulty.INTERMEDIATE, 1));

        list.add(new QuestionRequest(
                "In CI/CD pipeline automation, what does the term 'GitOps' primarily signify?",
                List.of("Using Git repositories as the single source of truth for declarative infrastructure and applications",
                        "Running git clone commands in GitHub Actions",
                        "Merging pull requests automatically without review",
                        "Storing database passwords directly inside git commits"),
                0, AssessmentDifficulty.INTERMEDIATE, 1));

        list.add(new QuestionRequest(
                "What is the primary purpose of a Kubernetes Ingress Controller?",
                List.of("Managing external HTTP/HTTPS access, SSL termination, and path-based routing to internal services",
                        "Scheduling Pods onto specific worker nodes",
                        "Storing persistent volume claims",
                        "Executing database backups automatically"),
                0, AssessmentDifficulty.ADVANCED, 2));

        list.add(new QuestionRequest(
                "In AWS IAM security best practices, what should be attached to EC2 instances or ECS tasks rather than hardcoding static access keys?",
                List.of("IAM Instance Profiles / Execution Roles", "Root Account Access Keys", "Access Key ID in environment variables", "SSH Key Pairs"),
                0, AssessmentDifficulty.BEGINNER, 1));

        return list;
    }

    private List<QuestionRequest> getAngularFrontendQuestions() {
        List<QuestionRequest> list = new ArrayList<>();
        list.add(new QuestionRequest(
                "In modern Angular (v16+), what reactive primitive allows fine-grained, zone-less reactivity and computed values?",
                List.of("Signals", "Promises", "Callbacks", "Two-way ngModel bindings"),
                0, AssessmentDifficulty.INTERMEDIATE, 2));

        list.add(new QuestionRequest(
                "In standalone Angular components, where are dependent components, directives, and modules explicitly imported?",
                List.of("In the 'imports' array of the @Component decorator", "In app.module.ts only", "In angular.json", "In index.html script tags"),
                0, AssessmentDifficulty.BEGINNER, 1));

        list.add(new QuestionRequest(
                "Which Angular Router guard interface is used to prevent unauthorized users from accessing a route?",
                List.of("canActivate", "canDeactivate", "resolve", "canMatch"),
                0, AssessmentDifficulty.BEGINNER, 1));

        list.add(new QuestionRequest(
                "What is the primary difference between RxJS switchMap and mergeMap in HTTP request handling?",
                List.of("switchMap cancels previous in-flight requests when a new event arrives, while mergeMap processes all concurrently",
                        "mergeMap cancels pending requests",
                        "switchMap cannot be used with HTTP observables",
                        "There is no difference; they are aliases"),
                0, AssessmentDifficulty.ADVANCED, 2));

        list.add(new QuestionRequest(
                "What is the correct syntax for two-way data binding in an Angular template with FormsModule?",
                List.of("[(ngModel)]=\"value\"", "([ngModel])=\"value\"", "{{ngModel: value}}", "[ngModel]=\"value\""),
                0, AssessmentDifficulty.BEGINNER, 1));

        return list;
    }

    private List<QuestionRequest> getBankingFintechQuestions() {
        List<QuestionRequest> list = new ArrayList<>();
        list.add(new QuestionRequest(
                "Which standard transaction ACID property guarantees that all database updates in a fund transfer occur completely or not at all?",
                List.of("Atomicity", "Consistency", "Isolation", "Durability"),
                0, AssessmentDifficulty.BEGINNER, 1));

        list.add(new QuestionRequest(
                "What international ISO standard defines the global messaging framework for cross-border wire transfers and financial transactions?",
                List.of("ISO 20022", "ISO 9001", "ISO 27001", "ISO 8583"),
                0, AssessmentDifficulty.INTERMEDIATE, 2));

        list.add(new QuestionRequest(
                "In PCI-DSS compliant payment processing, what technique replaces sensitive Primary Account Numbers (PAN) with non-exploitable identifiers?",
                List.of("Tokenization", "Base64 encoding", "MD5 hashing", "Symmetric XOR obfuscation"),
                0, AssessmentDifficulty.INTERMEDIATE, 1));

        list.add(new QuestionRequest(
                "In accounting and core banking ledger systems, why is the double-entry bookkeeping model mandatory?",
                List.of("Every transaction requires equal and offsetting debit and credit entries to maintain ledger balance and auditability",
                        "It allows transactions to be recorded twice for redundancy",
                        "It doubles transaction processing speed",
                        "It automatically refunds failed payments"),
                0, AssessmentDifficulty.BEGINNER, 1));

        return list;
    }

    private List<QuestionRequest> getDataAiQuestions() {
        List<QuestionRequest> list = new ArrayList<>();
        list.add(new QuestionRequest(
                "In Apache Kafka architecture, how are messages within a single partition ordered and stored?",
                List.of("Strictly in append-only sequential order with monotonic offsets",
                        "Randomly distributed based on consumer capacity",
                        "Sorted by timestamp in memory only",
                        "Ordered alphabetically by key"),
                0, AssessmentDifficulty.INTERMEDIATE, 2));

        list.add(new QuestionRequest(
                "What is the primary indicator of an overfitted machine learning model during validation?",
                List.of("High training accuracy coupled with significantly lower test / validation accuracy",
                        "Low training accuracy and low validation accuracy",
                        "Validation loss decreasing alongside training loss",
                        "Model inference time exceeding 1 second"),
                0, AssessmentDifficulty.BEGINNER, 1));

        list.add(new QuestionRequest(
                "In modern Large Language Models (LLMs), what architectural mechanism allows tokens to attend dynamically to relevant context across the sequence?",
                List.of("Multi-Head Self-Attention", "Recurrent LSTM gating", "Convolutional Max-Pooling", "Markov Decision Chains"),
                0, AssessmentDifficulty.ADVANCED, 2));

        return list;
    }

    private List<QuestionRequest> getGeneralArchitectureQuestions() {
        List<QuestionRequest> list = new ArrayList<>();
        list.add(new QuestionRequest(
                "According to the CAP theorem in distributed systems, what trade-off must a system make when network partitioning (P) occurs?",
                List.of("Choose between Consistency (C) and Availability (A)",
                        "Choose between Performance and Security",
                        "Choose between Latency and Throughput",
                        "Choose between Encryption and Compression"),
                0, AssessmentDifficulty.INTERMEDIATE, 1));

        list.add(new QuestionRequest(
                "What microservices resilience pattern temporarily halts calls to a failing downstream service to allow recovery?",
                List.of("Circuit Breaker", "Bulkhead", "Saga Pattern", "Event Sourcing"),
                0, AssessmentDifficulty.INTERMEDIATE, 1));

        list.add(new QuestionRequest(
                "Which HTTP method is defined by RFC 7231 as both safe and idempotent?",
                List.of("GET", "POST", "PATCH", "CONNECT"),
                0, AssessmentDifficulty.BEGINNER, 1));

        return list;
    }

    private QuestionRequest createSynthesizedQuestion(Course course, int questionNumber) {
        String title = course.getTitle();
        return new QuestionRequest(
                String.format("Regarding '%s', which of the following best represents an industry best practice for enterprise implementation?", title),
                List.of("Employing modular architecture, automated testing, continuous integration, and secure configuration",
                        "Disabling validation and logging to maximize raw execution speed",
                        "Storing application secrets and passwords directly inside version-controlled configuration files",
                        "Deploying monolithic architectures without automated health checks or rollback strategies"),
                0,
                AssessmentDifficulty.INTERMEDIATE,
                1
        );
    }
}
