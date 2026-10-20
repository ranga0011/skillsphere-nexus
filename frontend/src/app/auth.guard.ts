import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isAuthenticated || inject(Router).createUrlTree(['/login']);
};

export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated) {
    return router.createUrlTree(['/login']);
  }
  const allowedRoles = route.data['roles'] as string[];
  return allowedRoles.includes(auth.currentUser?.role ?? '')
    ? true
    : router.createUrlTree([auth.isHrAdmin ? '/hr' : '/employee']);
};

export const homeGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return router.createUrlTree(auth.isAuthenticated
    ? [auth.isHrAdmin ? '/hr' : '/employee']
    : ['/login']);
};