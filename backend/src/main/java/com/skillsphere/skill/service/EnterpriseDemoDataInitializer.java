package com.skillsphere.skill.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.Course;
import com.skillsphere.skill.entity.InternalJob;
import com.skillsphere.skill.entity.LearningPath;
import com.skillsphere.skill.repository.CourseRepository;
import com.skillsphere.skill.repository.InternalJobRepository;
import com.skillsphere.skill.repository.LearningPathRepository;

@Component
public class EnterpriseDemoDataInitializer implements ApplicationRunner {

    private final CourseRepository courseRepository;
    private final LearningPathRepository learningPathRepository;
    private final InternalJobRepository internalJobRepository;

    public EnterpriseDemoDataInitializer(CourseRepository courseRepository,
                                         LearningPathRepository learningPathRepository,
                                         InternalJobRepository internalJobRepository) {
        this.courseRepository = courseRepository;
        this.learningPathRepository = learningPathRepository;
        this.internalJobRepository = internalJobRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
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

        if (learningPathRepository.count() == 0) {
            LearningPath p1 = new LearningPath("Java Full Stack Developer Track", "Comprehensive track covering Java 25, Spring Boot 4, Angular 20, and PostgreSQL.", "Full Stack Developer", 4, 67, "Java 25, Spring Boot 4, Angular 20, PostgreSQL");
            learningPathRepository.save(p1);

            LearningPath p2 = new LearningPath("Cloud Architect & DevOps Mastery", "Production Kubernetes, AWS architecture, Prometheus metrics, and CI/CD automation.", "Cloud Solutions Architect", 5, 45, "AWS, Kubernetes, Docker, Terraform");
            learningPathRepository.save(p2);
        }

        if (internalJobRepository.count() == 0) {
            InternalJob j1 = new InternalJob("Tech Lead - Cloud Platforms", "Engineering", "Lead the engineering team building enterprise cloud microservices and event architecture.", "Java, Spring Boot, Angular, AWS", 3);
            internalJobRepository.save(j1);

            InternalJob j2 = new InternalJob("Senior DevOps & Site Reliability Engineer", "Infrastructure", "Architect and maintain Kubernetes clusters, Kafka streaming, and observability.", "Docker, Kubernetes, Kafka, CI/CD", 2);
            internalJobRepository.save(j2);

            InternalJob j3 = new InternalJob("Full Stack Software Engineer", "Core Banking", "Develop end-to-end features for core banking workflows and payments.", "Java, Angular, PostgreSQL, REST", 4);
            internalJobRepository.save(j3);
        }
    }
}
