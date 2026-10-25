import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CareerPlan, InternalJob, JobApplication } from './feature-models';

@Injectable({ providedIn: 'root' })
export class CareerService {
  private readonly apiUrl = 'http://localhost:8080/api/career';

  constructor(private http: HttpClient) {}

  getMyCareerPlan(): Observable<CareerPlan> {
    return this.http.get<CareerPlan>(`${this.apiUrl}/plan/me`);
  }

  saveMyCareerPlan(plan: CareerPlan): Observable<CareerPlan> {
    return this.http.post<CareerPlan>(`${this.apiUrl}/plan/me`, plan);
  }

  getAllJobs(): Observable<InternalJob[]> {
    return this.http.get<InternalJob[]>(`${this.apiUrl}/jobs`);
  }

  createJob(job: InternalJob): Observable<InternalJob> {
    return this.http.post<InternalJob>(`${this.apiUrl}/jobs`, job);
  }

  getMyApplications(): Observable<JobApplication[]> {
    return this.http.get<JobApplication[]>(`${this.apiUrl}/applications/me`);
  }

  applyForJob(jobId: number): Observable<JobApplication> {
    return this.http.post<JobApplication>(`${this.apiUrl}/jobs/${jobId}/apply`, {});
  }
}
