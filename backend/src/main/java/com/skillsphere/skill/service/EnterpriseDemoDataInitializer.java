package com.skillsphere.skill.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.CareerPlan;
import com.skillsphere.skill.entity.Certification;
import com.skillsphere.skill.entity.CertificationStatus;
import com.skillsphere.skill.entity.Course;
import com.skillsphere.skill.entity.Employee;
import com.skillsphere.skill.entity.InternalJob;
import com.skillsphere.skill.entity.LearningPath;
import com.skillsphere.skill.entity.Skill;
import com.skillsphere.skill.entity.Student;
import com.skillsphere.skill.entity.VerificationStatus;
import com.skillsphere.skill.repository.CareerPlanRepository;
import com.skillsphere.skill.repository.CertificationRepository;
import com.skillsphere.skill.repository.CourseRepository;
import com.skillsphere.skill.repository.EmployeeRepository;
import com.skillsphere.skill.repository.InternalJobRepository;
import com.skillsphere.skill.repository.LearningPathRepository;
import com.skillsphere.skill.repository.SkillRepository;
import com.skillsphere.skill.repository.StudentRepository;
import com.skillsphere.skill.security.AppUser;
import com.skillsphere.skill.security.AppUserRepository;
import com.skillsphere.skill.security.UserRole;

@Component
@org.springframework.context.annotation.Profile({"local", "docker", "demo"})
public class EnterpriseDemoDataInitializer implements ApplicationRunner {

    private final CourseRepository courseRepository;
    private final LearningPathRepository learningPathRepository;
    private final InternalJobRepository internalJobRepository;
    private final SkillRepository skillRepository;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;
    private final AppUserRepository appUserRepository;
    private final CareerPlanRepository careerPlanRepository;
    private final CertificationRepository certificationRepository;
    private final PasswordEncoder passwordEncoder;

    public EnterpriseDemoDataInitializer(CourseRepository courseRepository,
                                         LearningPathRepository learningPathRepository,
                                         InternalJobRepository internalJobRepository,
                                         SkillRepository skillRepository,
                                         EmployeeRepository employeeRepository,
                                         StudentRepository studentRepository,
                                         AppUserRepository appUserRepository,
                                         CareerPlanRepository careerPlanRepository,
                                         CertificationRepository certificationRepository,
                                         PasswordEncoder passwordEncoder) {
        this.courseRepository = courseRepository;
        this.learningPathRepository = learningPathRepository;
        this.internalJobRepository = internalJobRepository;
        this.skillRepository = skillRepository;
        this.employeeRepository = employeeRepository;
        this.studentRepository = studentRepository;
        this.appUserRepository = appUserRepository;
        this.careerPlanRepository = careerPlanRepository;
        this.certificationRepository = certificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Seed Skills
        if (skillRepository.count() == 0) {
            skillRepository.saveAll(List.of(
                new Skill("Java 25 & Spring Boot 4", "Backend", "Enterprise core framework, virtual threads, reactive and microservices architecture."),
                new Skill("Angular 20 & TypeScript", "Frontend", "Signals, Standalone components, state management, and modern responsive UI design."),
                new Skill("Kubernetes & Docker", "DevOps", "Containerization, cluster configuration, Helm charts, ingress, and production deployment."),
                new Skill("AWS Cloud Solutions", "Cloud", "VPC, ECS, Lambda, IAM policies, S3, and scalable enterprise cloud design."),
                new Skill("PostgreSQL & Database Optimization", "Data", "ACID transactions, relational modeling, indexing, and Flyway schema migrations."),
                new Skill("Kafka Event Streaming", "Architecture", "Distributed event messaging, topic partitioning, consumers, and event-driven architecture."),
                new Skill("REST & GraphQL APIs", "Integration", "High-performance API contracts, OpenAPI 3 specifications, and authentication."),
                new Skill("Agile Scrum & Technical Leadership", "Management", "Sprint planning, pair programming, technical backlog grooming, and code reviews.")
            ));
        }

        // Seed Courses
        if (courseRepository.count() == 0) {
            Course c1 = new Course("Spring Boot 4 Microservices & Cloud", "Enterprise microservices architecture, event streaming with Kafka, and containerized deployment.", 40, "Jane Doe", "ONLINE", 4.8, "Backend");
            c1.setTotalEnrolled(247);
            c1.setTotalCompleted(189);
            courseRepository.save(c1);

            Course c2 = new Course("Angular 20 Full-Stack Architecture", "Modern frontend engineering with Angular 20, Signals, Standalone Components, and Material design.", 35, "Alex Chen", "WORKSHOP", 4.9, "Frontend");
            c2.setTotalEnrolled(180);
            c2.setTotalCompleted(150);
            courseRepository.save(c2);

            Course c3 = new Course("Cloud & DevOps: AWS, Docker & Kubernetes", "Cloud native architecture, container orchestration with Kubernetes, CI/CD pipelines and Prometheus monitoring.", 45, "Sarah Miller", "BOOTCAMP", 4.7, "DevOps");
            c3.setTotalEnrolled(210);
            c3.setTotalCompleted(175);
            courseRepository.save(c3);

            Course c4 = new Course("Enterprise Banking Domain & Financial Systems", "Core banking workflows, compliance, regulatory reporting, and payment gateway security.", 30, "Michael Vance", "WEBINAR", 4.6, "Domain");
            c4.setTotalEnrolled(140);
            c4.setTotalCompleted(115);
            courseRepository.save(c4);
        }

        // Seed Learning Paths
        if (learningPathRepository.count() == 0) {
            LearningPath p1 = new LearningPath("Java Full Stack Developer Track", "Comprehensive track covering Java 25, Spring Boot 4, Angular 20, and PostgreSQL.", "Full Stack Developer", 4, 67, "Java 25, Spring Boot 4, Angular 20, PostgreSQL");
            learningPathRepository.save(p1);

            LearningPath p2 = new LearningPath("Cloud Architect & DevOps Mastery", "Production Kubernetes, AWS architecture, Prometheus metrics, and CI/CD automation.", "Cloud Solutions Architect", 5, 45, "AWS, Kubernetes, Docker, Terraform");
            learningPathRepository.save(p2);
        }

        // Seed Internal Jobs
        if (internalJobRepository.count() == 0) {
            InternalJob j1 = new InternalJob("Tech Lead - Cloud Platforms", "Engineering", "Lead the engineering team building enterprise cloud microservices and event architecture.", "Java, Spring Boot, Angular, AWS", 3);
            internalJobRepository.save(j1);

            InternalJob j2 = new InternalJob("Senior DevOps & Site Reliability Engineer", "Infrastructure", "Architect and maintain Kubernetes clusters, Kafka streaming, and observability.", "Docker, Kubernetes, Kafka, CI/CD", 2);
            internalJobRepository.save(j2);

            InternalJob j3 = new InternalJob("Full Stack Software Engineer", "Core Banking", "Develop end-to-end features for core banking workflows and payments.", "Java, Angular, PostgreSQL, REST", 4);
            internalJobRepository.save(j3);
        }

        // Seed Employee User & Career Track
        if (!appUserRepository.existsByUsername("employee@skillsphere.com")) {
            Employee emp = employeeRepository.findAll().stream().findFirst().orElseGet(() -> {
                Employee e = new Employee("Sarah Connor", "sarah.connor@skillsphere.com", "Software Engineer", "Cloud Platforms");
                return employeeRepository.save(e);
            });

            AppUser empUser = new AppUser("employee@skillsphere.com", passwordEncoder.encode("employee12345"), UserRole.EMPLOYEE, emp.getId());
            appUserRepository.save(empUser);

            if (careerPlanRepository.findByEmployeeId(emp.getId()).isEmpty()) {
                CareerPlan cp = new CareerPlan();
                cp.setEmployee(emp);
                cp.setCurrentRole("Software Engineer");
                cp.setTargetRole("Senior Solutions Architect");
                cp.setProgressPercentage(72);
                cp.setSkillGaps("Kubernetes CKA, Distributed Systems Architecture, Event Driven Design");
                cp.setMentorName("Dr. Robert Chen");
                cp.setPromotionReadinessScore(85);
                cp.setPromotionEligibility("ELIGIBLE_NEXT_CYCLE");
                cp.setStatus("ACTIVE");
                careerPlanRepository.save(cp);
            }

            if (certificationRepository.findByEmployee_Id(emp.getId()).isEmpty()) {
                Certification cert1 = new Certification();
                cert1.setName("AWS Certified Solutions Architect - Associate");
                cert1.setIssuingOrganization("Amazon Web Services");
                cert1.setCredentialId("AWS-883921");
                cert1.setEmployee(emp);
                cert1.setIssueDate(LocalDate.now().minusMonths(6));
                cert1.setExpiryDate(LocalDate.now().plusMonths(30));
                cert1.setStatus(CertificationStatus.ACTIVE);
                cert1.setVerificationStatus(VerificationStatus.VERIFIED);
                certificationRepository.save(cert1);

                Certification cert2 = new Certification();
                cert2.setName("Certified Kubernetes Administrator (CKA)");
                cert2.setIssuingOrganization("Cloud Native Computing Foundation");
                cert2.setCredentialId("CKA-29401");
                cert2.setEmployee(emp);
                cert2.setIssueDate(LocalDate.now().minusMonths(23));
                cert2.setExpiryDate(LocalDate.now().plusDays(18));
                cert2.setStatus(CertificationStatus.EXPIRING_SOON);
                cert2.setVerificationStatus(VerificationStatus.VERIFIED);
                certificationRepository.save(cert2);
            }
        }

        // Seed Student User
        if (!appUserRepository.existsByUsername("student@skillsphere.com")) {
            Student student = studentRepository.findAll().stream().findFirst().orElseGet(() -> {
                Student s = new Student("David Kim", "david.kim@skillsphere.com", "Full-Stack Cloud Graduate Program");
                return studentRepository.save(s);
            });

            AppUser studentUser = new AppUser("student@skillsphere.com", passwordEncoder.encode("student12345"), UserRole.STUDENT, null, student.getId());
            appUserRepository.save(studentUser);
        }
    }
}
