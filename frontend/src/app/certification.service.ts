import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Certification } from './feature-models';

@Injectable({ providedIn: 'root' })
export class CertificationService {
  private readonly apiUrl = 'http://localhost:8080/api/certifications';

  constructor(private http: HttpClient) {}

  getCertifications(): Observable<Certification[]> {
    return this.http.get<Certification[]>(this.apiUrl);
  }

  getCertificationsByEmployee(employeeId: number): Observable<Certification[]> {
    return this.http.get<Certification[]>(`${this.apiUrl}?employeeId=${employeeId}`);
  }

  createCertification(payload: Certification): Observable<Certification> {
    return this.http.post<Certification>(this.apiUrl, payload);
  }

  updateCertification(id: number, payload: Certification): Observable<Certification> {
    return this.http.put<Certification>(`${this.apiUrl}/${id}`, payload);
  }

  deleteCertification(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
