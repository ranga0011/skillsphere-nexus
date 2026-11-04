import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="login-page">
      <section class="login-panel" aria-labelledby="login-title">
        <div class="brand-mark" aria-hidden="true">S</div>
        <p class="eyebrow">SKILLSPHERE NEXUS</p>
        <h1 id="login-title">Sign in to your workspace</h1>
        <p class="intro">Use your organization account to continue.</p>
        <form (ngSubmit)="submit()" #loginForm="ngForm">
          <div class="form-group">
            <label for="username">Work email</label>
            <input id="username" name="username" type="email" [(ngModel)]="username" autocomplete="username" placeholder="name@skillsphere.com" required autofocus />
          </div>
          <div class="form-group">
            <label for="password">Password</label>
            <input id="password" name="password" type="password" [(ngModel)]="password" autocomplete="current-password" placeholder="••••••••" required />
          </div>
          <p class="form-error" role="alert" *ngIf="errorMessage">{{ errorMessage }}</p>
          <button type="submit" [disabled]="loginForm.invalid || loading">
            {{ loading ? 'Signing in…' : 'Sign in' }}
          </button>
        </form>
      </section>
      <footer class="login-footer">
        <span class="footer-label">Quick Sign-in:</span>
        <button type="button" class="footer-role-chip" (click)="quickFill('admin@skillsphere.com', 'admin123456789')">HR Admin</button>
        <span class="footer-sep">&bull;</span>
        <button type="button" class="footer-role-chip" (click)="quickFill('employee@skillsphere.com', 'employee12345')">Employee</button>
        <span class="footer-sep">&bull;</span>
        <button type="button" class="footer-role-chip" (click)="quickFill('student@skillsphere.com', 'student12345')">Student</button>
      </footer>
    </main>
  `,
  styleUrl: './login.scss'
})
export class LoginComponent {
  username = '';
  password = '';
  errorMessage = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {}

  quickFill(u: string, p: string): void {
    this.username = u;
    this.password = p;
    this.errorMessage = '';
  }

  submit(): void {
    if (this.loading) {
      return;
    }
    this.errorMessage = '';
    this.loading = true;
    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigateByUrl(
          this.auth.currentUser?.role === 'HR_ADMIN'
            ? '/hr'
            : this.auth.currentUser?.role === 'STUDENT'
              ? '/student'
              : '/employee'
        );
      },
      error: (error: unknown) => {
        this.loading = false;
        if (error instanceof HttpErrorResponse && error.status === 401) {
          this.errorMessage = 'Email or password is incorrect.';
        } else if (error instanceof HttpErrorResponse && error.status === 500) {
          this.errorMessage = 'Authentication service is not initialized. Restart the backend with DB_PASSWORD and JWT_SECRET configured.';
        } else {
          this.errorMessage = 'Sign-in is unavailable. Check that the backend is running and try again.';
        }
      }
    });
  }
}
