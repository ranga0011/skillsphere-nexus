import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = request.url.startsWith('http://localhost:8080/api/') ? auth.token : null;
  const authenticatedRequest = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authenticatedRequest).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401
          && request.url.startsWith('http://localhost:8080/api/')
          && !request.url.endsWith('/api/auth/login')) {
        auth.logout();
        void router.navigateByUrl('/login');
      } else if (error instanceof HttpErrorResponse && error.status === 403) {
        auth.setAuthorizationDenied();
      }
      return throwError(() => error);
    })
  );
};