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
          <label for="username">Work email</label>
          <input id="username" name="username" type="email" [(ngModel)]="username" autocomplete="username" required autofocus />
          <label for="password">Password</label>
          <input id="password" name="password" type="password" [(ngModel)]="password" autocomplete="current-password" required />
          <p class="form-error" role="alert" *ngIf="errorMessage">{{ errorMessage }}</p>
          <button type="submit" [disabled]="loginForm.invalid || loading">
            {{ loading ? 'Signing in…' : 'Sign in' }}
          </button>
        </form>
      </section>
      <p class="login-footer">Skills and competency management</p>
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

  submit(): void {
    if (this.loading) {
      return;
    }
    this.errorMessage = '';
    this.loading = true;
    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigateByUrl('/');
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