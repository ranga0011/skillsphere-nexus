import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EmployeeSkillAssignment } from './feature-models';

@Injectable({ providedIn: 'root' })
export class EmployeeSkillService {
  private readonly apiUrl = 'http://localhost:8080/api/employee-skills';

  constructor(private http: HttpClient) {}

  getAssignments(): Observable<EmployeeSkillAssignment[]> {
    return this.http.get<EmployeeSkillAssignment[]>(this.apiUrl);
  }

  getAssignmentsByEmployee(employeeId: number): Observable<EmployeeSkillAssignment[]> {
    return this.http.get<EmployeeSkillAssignment[]>(`${this.apiUrl}/employee/${employeeId}`);
  }

  createAssignment(payload: EmployeeSkillAssignment): Observable<EmployeeSkillAssignment> {
    return this.http.post<EmployeeSkillAssignment>(this.apiUrl, payload);
  }

  updateAssignment(id: number, payload: EmployeeSkillAssignment): Observable<EmployeeSkillAssignment> {
    return this.http.put<EmployeeSkillAssignment>(`${this.apiUrl}/${id}`, payload);
  }

  deleteAssignment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
