import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface AuthUser {
  username: string;
  role: 'HR_ADMIN' | 'EMPLOYEE' | 'STUDENT';
  employeeId: number | null;
  studentId: number | null;
}

interface LoginResponse extends AuthUser {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface CreateStudentAccountRequest {
  username: string;
  password: string;
  name: string;
  email: string;
  program: string;
}

export interface AccountCreatedResponse {
  username: string;
  role: string;
  employeeId: number | null;
  studentId: number | null;
}

interface AuthSession {
  user: AuthUser;
  token: string;
  expiresAt: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/auth';
  private readonly session = signal<AuthSession | null>(null);
  private readonly authorizationError = signal('');

  constructor(private http: HttpClient) {}

  get currentUser(): AuthUser | null {
    return this.activeSession()?.user ?? null;
  }

  get token(): string | null {
    return this.activeSession()?.token ?? null;
  }

  get isAuthenticated(): boolean {
    return this.activeSession() !== null;
  }

  get isHrAdmin(): boolean {
    return this.currentUser?.role === 'HR_ADMIN';
  }

  get isEmployee(): boolean {
    return this.currentUser?.role === 'EMPLOYEE';
  }

  get isStudent(): boolean {
    return this.currentUser?.role === 'STUDENT';
  }

  get authorizationMessage(): string {
    return this.authorizationError();
  }

  setAuthorizationDenied(): void {
    this.authorizationError.set('Your account is not permitted to perform that action.');
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { username, password }).pipe(
      tap((response) => this.session.set({
        user: {
          username: response.username,
          role: response.role,
          employeeId: response.employeeId,
          studentId: response.studentId
        },
        token: response.accessToken,
        expiresAt: Date.now() + response.expiresIn * 1000
      }))
    );
  }

  createStudentAccount(payload: CreateStudentAccountRequest): Observable<AccountCreatedResponse> {
    return this.http.post<AccountCreatedResponse>(`${this.apiUrl}/students`, payload);
  }

  logout(): void {
    this.session.set(null);
    this.authorizationError.set('');
  }

  private activeSession(): AuthSession | null {
    const session = this.session();
    if (session && Date.now() >= session.expiresAt) {
      this.session.set(null);
      return null;
    }
    return session;
  }
}