import { Routes } from '@angular/router';
import { WorkspaceComponent } from './app';
import { authGuard, homeGuard, roleGuard } from './auth.guard';
import { LoginComponent } from './login.component';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'hr', component: WorkspaceComponent, canActivate: [authGuard, roleGuard], data: { roles: ['HR_ADMIN'] } },
	{ path: 'employee', component: WorkspaceComponent, canActivate: [authGuard, roleGuard], data: { roles: ['EMPLOYEE'] } },
	{ path: '', pathMatch: 'full', component: WorkspaceComponent, canActivate: [homeGuard] },
	{ path: '**', redirectTo: '' }
];
