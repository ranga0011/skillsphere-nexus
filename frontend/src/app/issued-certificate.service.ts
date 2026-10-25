import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { IssuedCertificate } from './feature-models';

@Injectable({ providedIn: 'root' })
export class IssuedCertificateService {
  private readonly apiUrl = 'http://localhost:8080/api/certificates';

  constructor(private http: HttpClient) {}

  getCertificates(): Observable<IssuedCertificate[]> {
    return this.http.get<IssuedCertificate[]>(this.apiUrl);
  }

  downloadCertificate(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/download`, { responseType: 'blob' });
  }

  revokeCertificate(id: number, reason: string): Observable<IssuedCertificate> {
    return this.http.put<IssuedCertificate>(`${this.apiUrl}/${id}/revoke`, { reason });
  }

  saveDownload(certificate: IssuedCertificate): Observable<void> {
    return this.downloadCertificate(certificate.id).pipe(map((blob): void => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `SkillSphere-Certificate-${certificate.certificateNumber}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    }));
  }
}
