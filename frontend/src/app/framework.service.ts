import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CompetencyFramework, FrameworkSkillRequirement, CompetencyGap } from './feature-models';

@Injectable({ providedIn: 'root' })
export class FrameworkService {
  private readonly apiUrl = 'http://localhost:8080/api/frameworks';

  constructor(private http: HttpClient) {}

  getFrameworks(): Observable<CompetencyFramework[]> {
    return this.http.get<CompetencyFramework[]>(this.apiUrl);
  }

  createFramework(payload: CompetencyFramework): Observable<CompetencyFramework> {
    return this.http.post<CompetencyFramework>(this.apiUrl, payload);
  }

  updateFramework(id: number, payload: CompetencyFramework): Observable<CompetencyFramework> {
    return this.http.put<CompetencyFramework>(`${this.apiUrl}/${id}`, payload);
  }

  deleteFramework(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getRequirements(frameworkId: number): Observable<FrameworkSkillRequirement[]> {
    return this.http.get<FrameworkSkillRequirement[]>(`${this.apiUrl}/${frameworkId}/requirements`);
  }

  addRequirement(frameworkId: number, payload: FrameworkSkillRequirement): Observable<FrameworkSkillRequirement> {
    return this.http.post<FrameworkSkillRequirement>(`${this.apiUrl}/${frameworkId}/requirements`, payload);
  }

  updateRequirement(frameworkId: number, requirementId: number, payload: FrameworkSkillRequirement): Observable<FrameworkSkillRequirement> {
    return this.http.put<FrameworkSkillRequirement>(`${this.apiUrl}/${frameworkId}/requirements/${requirementId}`, payload);
  }

  deleteRequirement(frameworkId: number, requirementId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${frameworkId}/requirements/${requirementId}`);
  }

  getGapReport(frameworkId: number, employeeId: number): Observable<CompetencyGap[]> {
    return this.http.get<CompetencyGap[]>(`${this.apiUrl}/${frameworkId}/gap-report/${employeeId}`);
  }
}
