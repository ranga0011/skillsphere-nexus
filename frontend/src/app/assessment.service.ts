import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Assessment } from './feature-models';

@Injectable({ providedIn: 'root' })
export class AssessmentService {
  private readonly apiUrl = 'http://localhost:8080/api/assessments';

  constructor(private http: HttpClient) {}

  getAssessments(): Observable<Assessment[]> {
    return this.http.get<Assessment[]>(this.apiUrl);
  }

  getAssessmentsByEmployee(employeeId: number): Observable<Assessment[]> {
    return this.http.get<Assessment[]>(`${this.apiUrl}/employee/${employeeId}`);
  }

  createAssessment(payload: Assessment): Observable<Assessment> {
    return this.http.post<Assessment>(this.apiUrl, payload);
  }

  updateAssessment(id: number, payload: Assessment): Observable<Assessment> {
    return this.http.put<Assessment>(`${this.apiUrl}/${id}`, payload);
  }

  verifyAssessment(id: number, status: 'VERIFIED' | 'REJECTED'): Observable<Assessment> {
    return this.http.put<Assessment>(`${this.apiUrl}/${id}/verification`, { verificationStatus: status });
  }

  deleteAssessment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
