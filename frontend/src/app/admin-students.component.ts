import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Student } from './feature-models';
import { StudentService } from './student.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-students',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="admin-students">
      <header class="topbar">
        <div>
          <h1>Student accounts</h1>
          <p>Create separate student profiles and issue their initial sign-in credentials.</p>
        </div>
        <button class="secondary-btn" type="button" (click)="goBack()">Back to workspace</button>
      </header>

      <p class="error-message" role="alert" *ngIf="errorMessage">{{ errorMessage }}</p>
      <p class="success-message" role="status" *ngIf="successMessage">{{ successMessage }}</p>

      <section class="panel">
        <h2>Create a student account</h2>
        <form #studentForm="ngForm" (ngSubmit)="createStudent()">
          <div class="form-grid">
            <label>Student name
              <input name="name" [(ngModel)]="form.name" required maxlength="100" />
            </label>
            <label>Student email
              <input name="email" type="email" [(ngModel)]="form.email" required email maxlength="150" />
            </label>
            <label>Program
              <input name="program" [(ngModel)]="form.program" required maxlength="150" />
            </label>
            <label>Sign-in username
              <input name="username" type="email" [(ngModel)]="form.username" required email maxlength="150" />
            </label>
            <label>Temporary password
              <input name="password" type="password" [(ngModel)]="form.password" required minlength="12" maxlength="72" autocomplete="new-password" />
            </label>
          </div>
          <p class="helper">Student credentials receive only the STUDENT role. Share passwords through an approved secure channel.</p>
          <button class="primary-btn" type="submit" [disabled]="studentForm.invalid || saving">
            {{ saving ? 'Creating…' : 'Create student account' }}
          </button>
        </form>
      </section>

      <section class="panel">
        <div class="panel-heading">
          <div><h2>Student directory</h2><p>Student profiles are separate from employee records.</p></div>
          <button class="secondary-btn" type="button" (click)="loadStudents()">Refresh</button>
        </div>
        <p *ngIf="loading">Loading students...</p>
        <div class="table-wrap" *ngIf="!loading">
          <table>
            <thead><tr><th>NAME</th><th>EMAIL</th><th>PROGRAM</th></tr></thead>
            <tbody>
              <tr *ngFor="let student of students">
                <td>{{ student.name }}</td>
                <td>{{ student.email }}</td>
                <td>{{ student.program }}</td>
              </tr>
              <tr *ngIf="students.length === 0"><td colspan="3">No student profiles have been created.</td></tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; padding: 28px; background: #f5f7fb; color: #17243a; font-family: "Segoe UI", Arial, sans-serif; }
    .admin-students { max-width: 1200px; margin: 0 auto; }
    .topbar, .panel-heading { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
    .panel { margin: 20px 0; padding: 24px; background: #fff; border: 1px solid #e1e7f0; border-radius: 12px; }
    .form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; margin-bottom: 18px; }
    label { display: flex; flex-direction: column; gap: 7px; font-size: 14px; }
    input { min-height: 42px; padding: 10px; border: 1px solid #cbd5e1; border-radius: 7px; font: inherit; }
    .primary-btn, .secondary-btn { min-height: 40px; padding: 9px 14px; border: 0; border-radius: 7px; font: inherit; font-weight: 600; cursor: pointer; }
    .primary-btn { background: #285ca8; color: #fff; }
    .secondary-btn { background: #e8eef8; color: #203e68; }
    button:disabled { opacity: .55; cursor: not-allowed; }
    .helper { color: #64748b; font-size: 13px; }
    table { width: 100%; border-collapse: collapse; text-align: left; }
    th, td { padding: 12px; border-bottom: 1px solid #e5eaf2; }
    th { background: #f2f5fa; color: #42536b; font-size: 12px; }
    .table-wrap { overflow-x: auto; }
    .error-message, .success-message { padding: 12px; border-radius: 7px; }
    .error-message { background: #fff0f0; color: #a61b1b; }
    .success-message { background: #e3f7ed; color: #11643f; }
    @media (max-width: 650px) { :host { padding: 16px; } .topbar, .panel-heading { align-items: flex-start; flex-direction: column; } .form-grid { grid-template-columns: 1fr; } .panel { padding: 16px; } }
  `]
})
export class AdminStudentsComponent implements OnInit {
  students: Student[] = [];
  loading = true;
  saving = false;
  errorMessage = '';
  successMessage = '';
  form = { username: '', password: '', name: '', email: '', program: '' };

  constructor(
    private readonly authService: AuthService,
    private readonly studentService: StudentService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadStudents();
  }

  loadStudents(): void {
    this.loading = true;
    this.studentService.getStudents().subscribe({
      next: (students) => {
        this.students = students;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Could not load the student directory.';
      }
    });
  }

  createStudent(): void {
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.authService.createStudentAccount(this.form).subscribe({
      next: (response) => {
        this.saving = false;
        this.form = { username: '', password: '', name: '', email: '', program: '' };
        this.successMessage = `Student account created with the ${response.role} role.`;
        this.loadStudents();
      },
      error: (error: HttpErrorResponse) => {
        this.saving = false;
        if (error.status === 409) {
          this.errorMessage = 'That username or student email is already registered.';
        } else if (error.status === 400) {
          const backendMessage = error.error?.message;
          this.errorMessage = typeof backendMessage === 'string' && backendMessage.trim()
            ? backendMessage
            : 'Check that all fields are valid and the password is 12–72 UTF-8 bytes.';
        } else if (error.status === 401) {
          this.errorMessage = 'Your session has expired. Sign in again as an HR administrator.';
        } else if (error.status === 403) {
          this.errorMessage = 'Only an HR administrator can create student accounts.';
        } else {
          this.errorMessage = `Could not create the student account (HTTP ${error.status || 'unknown'}). Check that the backend is running.`;
        }
      }
    });
  }

  goBack(): void {
    void this.router.navigateByUrl('/hr');
  }
}
