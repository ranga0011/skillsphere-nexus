import { Routes } from '@angular/router';
import { WorkspaceComponent } from './app';
import { authGuard, homeGuard, roleGuard } from './auth.guard';
import { LoginComponent } from './login.component';
import { StudentDashboardComponent } from './student-dashboard.component';
import { AdminStudentsComponent } from './admin-students.component';
import { AdminLearningComponent } from './admin-learning.component';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'hr', pathMatch: 'full', component: WorkspaceComponent, canActivate: [authGuard, roleGuard], data: { roles: ['HR_ADMIN'] } },
	{ path: 'hr/students', component: AdminStudentsComponent, canActivate: [authGuard, roleGuard], data: { roles: ['HR_ADMIN'] } },
	{ path: 'hr/learning', component: AdminLearningComponent, canActivate: [authGuard, roleGuard], data: { roles: ['HR_ADMIN'] } },
	{ path: 'employee', pathMatch: 'full', component: WorkspaceComponent, canActivate: [authGuard, roleGuard], data: { roles: ['EMPLOYEE'] } },
	{ path: 'student', component: StudentDashboardComponent, canActivate: [authGuard, roleGuard], data: { roles: ['STUDENT'] } },
	{ path: '', pathMatch: 'full', component: WorkspaceComponent, canActivate: [homeGuard] },
	{ path: '**', redirectTo: '' }
];
