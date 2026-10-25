import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Assessment,
  CompetencyGapReport,
  EmployeeSkillAssignment,
  Student
} from './feature-models';

@Injectable({ providedIn: 'root' })
export class StudentService {
  private readonly apiUrl = 'http://localhost:8080/api/students';

  constructor(private http: HttpClient) {}

  getStudents(): Observable<Student[]> {
    return this.http.get<Student[]>(this.apiUrl);
  }

  getMyProfile(): Observable<Student> {
    return this.http.get<Student>(`${this.apiUrl}/me`);
  }

  updateMyProfile(payload: Pick<Student, 'name' | 'email' | 'program'>): Observable<Student> {
    return this.http.put<Student>(`${this.apiUrl}/me`, payload);
  }

  getMySkills(): Observable<EmployeeSkillAssignment[]> {
    return this.http.get<EmployeeSkillAssignment[]>(`${this.apiUrl}/me/skills`);
  }

  requestSkill(skillId: number, proficiency: number): Observable<EmployeeSkillAssignment> {
    return this.http.post<EmployeeSkillAssignment>(`${this.apiUrl}/me/skills`, { skillId, proficiency });
  }

  getMyAssessments(): Observable<Assessment[]> {
    return this.http.get<Assessment[]>(`${this.apiUrl}/me/assessments`);
  }

  getMyGapReport(frameworkId: number): Observable<CompetencyGapReport> {
    return this.http.get<CompetencyGapReport>(
      `http://localhost:8080/api/frameworks/${frameworkId}/gap-report/me`
    );
  }
}
