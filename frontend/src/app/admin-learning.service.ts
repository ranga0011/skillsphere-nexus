import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AssessmentQuestion, LearnerAssessment } from './learning.service';

export interface AdminUser {
  id: number;
  username: string;
  role: string;
  employeeId: number | null;
  studentId: number | null;
  enabled: boolean;
}

export interface Paged<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface GapAnalytics {
  frameworkId: number;
  frameworkName: string;
  totalLearners: number;
  learnersMeetingAllRequirements: number;
  learnersMeetingPercentage: number;
  requiredSkills: number;
  certificateExpiry: {
    activeCertificates: number;
    expiringWithin30Days: number;
    expiredCertificates: number;
  };
  skills: Array<{
    skillId: number;
    skillName: string;
    requiredProficiency: number;
    learnersWithGap: number;
    learnersWithGapPercentage: number;
    totalGapLevels: number;
    awaitingAssessment: number;
    awaitingVerification: number;
  }>;
  totalSkillEntries: number;
  page: number;
  size: number;
}

export interface ResourcePayload {
  title: string;
  description: string;
  skillId: number;
  resourceType: string;
  difficultyLevel: string;
  verifiedUrl: string;
  active: boolean;
}

export interface AdminResource {
  id: number;
  title: string;
  description: string;
  skillId: number;
  skillName: string;
  resourceType: string;
  difficultyLevel: string;
  verifiedUrl: string | null;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class AdminLearningService {
  private readonly adminUrl = 'http://localhost:8080/api/admin';

  constructor(private readonly http: HttpClient) {}

  getUsers(page: number): Observable<Paged<AdminUser>> {
    return this.http.get<Paged<AdminUser>>(`${this.adminUrl}/users`, { params: { page, size: 25 } });
  }

  setUserEnabled(userId: number, enabled: boolean): Observable<unknown> {
    return this.http.put(`${this.adminUrl}/users/${userId}/status`, { enabled });
  }

  getAssessments(): Observable<LearnerAssessment[]> {
    return this.http.get<LearnerAssessment[]>(`${this.adminUrl}/learning/assessments`);
  }

  createAssessment(payload: {
    title: string;
    instructions: string;
    skillId: number;
    passPercentage: number;
    maxAttempts: number;
    active?: boolean;
  }): Observable<LearnerAssessment> {
    return this.http.post<LearnerAssessment>(`${this.adminUrl}/learning/assessments`, payload);
  }

  updateAssessment(id: number, payload: {
    title: string;
    instructions: string;
    skillId: number;
    passPercentage: number;
    maxAttempts: number;
    active: boolean;
  }): Observable<LearnerAssessment> {
    return this.http.put<LearnerAssessment>(`${this.adminUrl}/learning/assessments/${id}`, payload);
  }

  createQuestion(assessmentId: number, payload: {
    prompt: string;
    options: string[];
    correctOptionIndex: number;
    difficulty: string;
    points: number;
  }): Observable<LearnerAssessment> {
    return this.http.post<LearnerAssessment>(
      `${this.adminUrl}/learning/assessments/${assessmentId}/questions`, payload
    );
  }

  getQuestions(assessmentId: number): Observable<AssessmentQuestion[]> {
    return this.http.get<AssessmentQuestion[]>(
      `${this.adminUrl}/learning/assessments/${assessmentId}/questions`
    );
  }

  getAnalytics(frameworkId: number, page: number, skillFilter: string): Observable<GapAnalytics> {
    return this.http.get<GapAnalytics>(`${this.adminUrl}/analytics/skill-gaps`, {
      params: { frameworkId, page, size: 25, skillFilter }
    });
  }

  getResources(): Observable<AdminResource[]> {
    return this.http.get<AdminResource[]>(`${this.adminUrl}/learning/resources`);
  }

  createResource(payload: ResourcePayload): Observable<AdminResource> {
    return this.http.post<AdminResource>(`${this.adminUrl}/learning/resources`, payload);
  }

  updateResource(id: number, payload: ResourcePayload): Observable<AdminResource> {
    return this.http.put<AdminResource>(`${this.adminUrl}/learning/resources/${id}`, payload);
  }
}
