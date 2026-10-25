import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { IssuedCertificate } from './feature-models';

export interface LearnerAssessment {
  id: number;
  title: string;
  instructions: string;
  skillId: number;
  skillName: string;
  passPercentage: number;
  maxAttempts: number;
  questionCount: number;
  attemptsRemaining: number;
  active: boolean;
}

export interface AssessmentQuestion {
  id: number;
  prompt: string;
  options: string[];
  difficulty: string;
  points: number;
  selectedOptionIndex?: number;
}

export interface StartedAssessment {
  attemptId: number;
  attemptNumber: number;
  startedAt: string;
  assessment: LearnerAssessment;
  questions: AssessmentQuestion[];
}

export interface LearnerAttemptResult {
  attemptId: number;
  assessmentId: number;
  assessmentTitle: string;
  status: 'IN_PROGRESS' | 'SUBMITTED';
  startedAt: string;
  submittedAt: string;
  attemptNumber: number;
  scorePoints: number;
  maxPoints: number;
  percentage: number;
  passed: boolean;
  passPercentage: number;
  certificate: IssuedCertificate | null;
}

export interface LearningResourceRecommendation {
  resource: {
    id: number;
    title: string;
    description: string;
    skillId: number;
    skillName: string;
    resourceType: string;
    difficultyLevel: string;
    verifiedUrl: string | null;
    active: boolean;
  };
  priorityGap: number;
  currentVerifiedProficiency: number;
  requiredProficiency: number;
}

@Injectable({ providedIn: 'root' })
export class LearningService {
  private readonly apiUrl = 'http://localhost:8080/api/learning';

  constructor(private readonly http: HttpClient) {}

  getAvailableAssessments(): Observable<LearnerAssessment[]> {
    return this.http.get<LearnerAssessment[]>(`${this.apiUrl}/assessments`);
  }

  startAssessment(id: number): Observable<StartedAssessment> {
    return this.http.post<StartedAssessment>(`${this.apiUrl}/assessments/${id}/attempts`, {});
  }

  submitAssessment(attempt: StartedAssessment): Observable<LearnerAttemptResult> {
    return this.http.post<LearnerAttemptResult>(`${this.apiUrl}/attempts/${attempt.attemptId}/submit`, {
      answers: attempt.questions.map(question => ({
        questionId: question.id,
        selectedOptionIndex: question.selectedOptionIndex
      }))
    });
  }

  getAttemptHistory(): Observable<LearnerAttemptResult[]> {
    return this.http.get<LearnerAttemptResult[]>(`${this.apiUrl}/attempts`);
  }

  getRecommendations(frameworkId: number): Observable<LearningResourceRecommendation[]> {
    return this.http.get<LearningResourceRecommendation[]>(
      `${this.apiUrl}/resources/recommendations`, { params: { frameworkId } }
    );
  }
}
