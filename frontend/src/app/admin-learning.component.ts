import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FrameworkService } from './framework.service';
import { SkillService } from './skill.service';
import { Skill } from './skill';
import { CompetencyFramework } from './feature-models';
import { AdminLearningService, AdminResource, AdminUser, GapAnalytics, ResourcePayload } from './admin-learning.service';
import { LearnerAssessment } from './learning.service';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-admin-learning',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="admin-learning">
      <header class="page-heading">
        <div>
          <p class="eyebrow">SKILLSPHERE NEXUS · ADMINISTRATION</p>
          <h1>Learning and account management</h1>
          <p>Manage learner assessments, accounts, resources, and organization-level skill gaps.</p>
        </div>
        <button type="button" class="secondary-btn" (click)="back()">Back to workspace</button>
      </header>
      <nav class="tabs" aria-label="Admin sections">
        <button type="button" [class.active]="tab === 'assessments'" (click)="tab = 'assessments'">Question bank</button>
        <button type="button" [class.active]="tab === 'users'" (click)="tab = 'users'">User accounts</button>
        <button type="button" [class.active]="tab === 'analytics'" (click)="tab = 'analytics'">Gap analytics</button>
        <button type="button" [class.active]="tab === 'resources'" (click)="tab = 'resources'">Learning resources</button>
      </nav>
      <p class="error-message" role="alert" *ngIf="error">{{ error }}</p>
      <p class="success-message" role="status" *ngIf="success">{{ success }}</p>

      <section class="panel" *ngIf="tab === 'assessments'">
        <h2>Create an assessment</h2>
        <form #assessmentForm="ngForm" (ngSubmit)="createAssessment()">
          <div class="form-grid">
            <label>Title<input name="title" [(ngModel)]="assessmentDraft.title" required maxlength="150" /></label>
            <label>Skill
              <select name="assessmentSkill" [(ngModel)]="assessmentDraft.skillId" required>
                <option [ngValue]="null" disabled>Select skill</option>
                <option *ngFor="let skill of skills" [ngValue]="skill.id">{{ skill.name }}</option>
              </select>
            </label>
            <label>Pass threshold (%)<input type="number" name="passPercentage" [(ngModel)]="assessmentDraft.passPercentage" min="1" max="100" required /></label>
            <label>Maximum attempts<input type="number" name="maxAttempts" [(ngModel)]="assessmentDraft.maxAttempts" min="1" max="10" required /></label>
          </div>
          <label>Instructions<textarea name="instructions" [(ngModel)]="assessmentDraft.instructions" required maxlength="1000"></textarea></label>
          <button class="primary-btn" type="submit" [disabled]="assessmentForm.invalid || saving">Create assessment</button>
        </form>
        <div class="form-grid question-maker" *ngIf="assessments.length">
          <label>Assessment
            <select name="questionAssessment" [(ngModel)]="selectedAssessmentId" (ngModelChange)="loadQuestions()">
              <option *ngFor="let assessment of assessments" [ngValue]="assessment.id">{{ assessment.title }} ({{ assessment.questionCount }} questions)</option>
            </select>
          </label>
          <label>Question prompt<textarea name="questionPrompt" [(ngModel)]="questionDraft.prompt" required maxlength="1000"></textarea></label>
          <label *ngFor="let option of questionDraft.options; let index = index">Option {{ index + 1 }}
            <input [name]="'option' + index" [(ngModel)]="questionDraft.options[index]" required maxlength="500" />
          </label>
          <label>Correct option
            <select name="correctOption" [(ngModel)]="questionDraft.correctOptionIndex">
              <option [ngValue]="0">Option 1</option><option [ngValue]="1">Option 2</option>
              <option [ngValue]="2">Option 3</option><option [ngValue]="3">Option 4</option>
            </select>
          </label>
          <label>Difficulty
            <select name="difficulty" [(ngModel)]="questionDraft.difficulty">
              <option value="BEGINNER">Beginner</option><option value="INTERMEDIATE">Intermediate</option><option value="ADVANCED">Advanced</option>
            </select>
          </label>
          <label>Points<input name="points" type="number" [(ngModel)]="questionDraft.points" min="1" max="10" /></label>
          <button class="primary-btn" type="button" (click)="addQuestion()" [disabled]="saving || !selectedAssessmentId">Add question</button>
        </div>
        <h2>Assessment bank</h2>
        <p *ngIf="assessments.length === 0">No assessments have been configured yet.</p>
        <article class="list-card" *ngFor="let assessment of assessments">
          <div><strong>{{ assessment.title }}</strong><span>{{ assessment.skillName }} · {{ assessment.questionCount }} questions · pass {{ assessment.passPercentage }}% · {{ assessment.attemptsRemaining }} attempts shown to learners</span><small>{{ assessment.instructions }}</small></div>
          <div class="status-actions"><span class="status-pill" [class.success]="assessment.active" [class.warning]="!assessment.active">{{ assessment.active ? 'ACTIVE' : 'INACTIVE' }}</span>
            <button class="secondary-btn" type="button" (click)="toggleAssessment(assessment)">{{ assessment.active ? 'Deactivate' : 'Activate' }}</button></div>
        </article>
        <p *ngIf="questions.length">Questions in selected assessment: {{ questions.length }}. Correct answers are not included in learner question payloads.</p>
      </section>

      <section class="panel" *ngIf="tab === 'users'">
        <div class="panel-heading"><div><h2>User accounts</h2><p>Deactivated accounts are blocked at login and on every authenticated API request.</p></div>
          <button class="secondary-btn" type="button" (click)="loadUsers()">Refresh</button></div>
        <div class="table-wrap"><table><thead><tr><th>USERNAME</th><th>ROLE</th><th>PROFILE</th><th>STATUS</th><th>ACTION</th></tr></thead>
          <tbody><tr *ngFor="let user of users">
            <td>{{ user.username }}</td><td>{{ user.role }}</td>
            <td>{{ user.studentId ? 'Student #' + user.studentId : (user.employeeId ? 'Employee #' + user.employeeId : 'Administrator') }}</td>
            <td>{{ user.enabled ? 'Active' : 'Deactivated' }}</td>
            <td><button type="button" class="secondary-btn" (click)="setUserStatus(user)" [disabled]="saving || user.username === currentUsername && user.enabled">{{ user.enabled ? 'Deactivate' : 'Reactivate' }}</button></td>
          </tr><tr *ngIf="users.length === 0"><td colspan="5">No user accounts on this page.</td></tr></tbody>
        </table></div>
        <div class="pagination"><button type="button" class="secondary-btn" (click)="changeUserPage(-1)" [disabled]="userPage === 0">Previous</button><span>Page {{ userPage + 1 }} of {{ userTotalPages || 1 }}</span><button type="button" class="secondary-btn" (click)="changeUserPage(1)" [disabled]="userPage + 1 >= userTotalPages">Next</button></div>
        <p>Self-deactivation and deactivating the last active HR administrator are blocked by the server.</p>
      </section>

      <section class="panel" *ngIf="tab === 'analytics'">
        <h2>Organization skill-gap analytics</h2>
        <div class="inline-form">
          <label>Framework<select name="analyticsFramework" [(ngModel)]="analyticsFrameworkId" (ngModelChange)="loadAnalytics()">
            <option [ngValue]="null">Choose a framework</option><option *ngFor="let framework of frameworks" [ngValue]="framework.id">{{ framework.name }}</option>
          </select></label>
          <label>Filter skill<input name="skillFilter" [(ngModel)]="skillFilter" (ngModelChange)="loadAnalytics()" /></label>
        </div>
        <ng-container *ngIf="analytics as report">
          <div class="summary-grid">
            <article><strong>{{ report.totalLearners }}</strong><span>learners</span></article>
            <article><strong>{{ report.learnersMeetingAllRequirements }} ({{ report.learnersMeetingPercentage | number:'1.0-1' }}%)</strong><span>meet every requirement</span></article>
            <article><strong>{{ report.requiredSkills }}</strong><span>required skills</span></article>
            <article><strong>{{ report.certificateExpiry.expiringWithin30Days }}</strong><span>certificates expiring in 30 days</span></article>
            <article><strong>{{ report.certificateExpiry.expiredCertificates }}</strong><span>expired certificates</span></article>
          </div>
          <div class="table-wrap"><table><thead><tr><th>SKILL</th><th>GAP LEARNERS</th><th>GAP %</th><th>GAP LEVELS</th><th>NEEDS ASSESSMENT</th><th>AWAITING VERIFICATION</th></tr></thead>
            <tbody><tr *ngFor="let item of report.skills"><td>{{ item.skillName }} ({{ item.requiredProficiency }}/10)</td><td>{{ item.learnersWithGap }}</td><td>{{ item.learnersWithGapPercentage | number:'1.0-1' }}%</td><td>{{ item.totalGapLevels }}</td><td>{{ item.awaitingAssessment }}</td><td>{{ item.awaitingVerification }}</td></tr>
              <tr *ngIf="report.skills.length === 0"><td colspan="6">No matching skill gaps for this filter.</td></tr>
            </tbody></table></div>
          <div class="pagination"><button type="button" class="secondary-btn" (click)="changeAnalyticsPage(-1)" [disabled]="analyticsPage === 0">Previous</button><span>Page {{ analyticsPage + 1 }} · {{ report.totalSkillEntries }} skills in filter</span><button type="button" class="secondary-btn" (click)="changeAnalyticsPage(1)" [disabled]="(analyticsPage + 1) * report.size >= report.totalSkillEntries">Next</button></div>
          <p>Gap percentages use all employee and student profiles as the denominator. Readiness requires verified proficiency at or above every framework skill level.</p>
        </ng-container>
        <p *ngIf="analyticsFrameworkId === null">Select a framework to load administrative aggregate reports.</p>
      </section>

      <section class="panel" *ngIf="tab === 'resources'">
        <h2>Add a learning resource</h2>
        <p>Only administrator-entered URLs are shown; no external course catalog is implied.</p>
        <form #resourceForm="ngForm" (ngSubmit)="createResource()">
          <div class="form-grid">
            <label>Title<input name="resourceTitle" [(ngModel)]="resourceDraft.title" required maxlength="150" /></label>
            <label>Skill<select name="resourceSkill" [(ngModel)]="resourceDraft.skillId" required><option [ngValue]="null" disabled>Select skill</option><option *ngFor="let skill of skills" [ngValue]="skill.id">{{ skill.name }}</option></select></label>
            <label>Type<input name="resourceType" [(ngModel)]="resourceDraft.resourceType" required maxlength="30" placeholder="Article, video, course" /></label>
            <label>Difficulty<input name="resourceDifficulty" [(ngModel)]="resourceDraft.difficultyLevel" required maxlength="20" placeholder="Beginner" /></label>
            <label>Verified URL (optional)<input name="resourceUrl" type="url" [(ngModel)]="resourceDraft.verifiedUrl" /></label>
          </div>
          <label>Description<textarea name="resourceDescription" [(ngModel)]="resourceDraft.description" required maxlength="1000"></textarea></label>
          <button class="primary-btn" type="submit" [disabled]="resourceForm.invalid || saving">Save resource</button>
        </form>
        <h2>Configured resources</h2>
        <article class="list-card" *ngFor="let resource of resources">
          <div><strong>{{ resource.title }} · {{ resource.skillName }}</strong><span>{{ resource.resourceType }} · {{ resource.difficultyLevel }} · {{ resource.active ? 'Active' : 'Inactive' }}</span><small>{{ resource.description }}</small><a *ngIf="resource.verifiedUrl" [href]="resource.verifiedUrl" target="_blank" rel="noopener noreferrer">{{ resource.verifiedUrl }}</a></div>
          <button class="secondary-btn" type="button" (click)="toggleResource(resource)">{{ resource.active ? 'Deactivate' : 'Activate' }}</button>
        </article>
        <p *ngIf="resources.length === 0">No resources configured yet; learners will be told when a gap has no matching resource.</p>
      </section>
    </main>
  `,
  styleUrl: './admin-learning.component.scss'
})
export class AdminLearningComponent implements OnInit {
  tab: 'assessments' | 'users' | 'analytics' | 'resources' = 'assessments';
  skills: Skill[] = [];
  frameworks: CompetencyFramework[] = [];
  assessments: LearnerAssessment[] = [];
  questions: Array<{ id: number; prompt: string; difficulty: string }> = [];
  users: AdminUser[] = [];
  resources: AdminResource[] = [];
  analytics: GapAnalytics | null = null;
  selectedAssessmentId: number | null = null;
  analyticsFrameworkId: number | null = null;
  analyticsPage = 0;
  skillFilter = '';
  userPage = 0;
  userTotalPages = 0;
  currentUsername = '';
  saving = false;
  error = '';
  success = '';
  assessmentDraft = { title: '', instructions: '', skillId: null as number | null, passPercentage: 70, maxAttempts: 3 };
  questionDraft = { prompt: '', options: ['', '', '', ''], correctOptionIndex: 0, difficulty: 'BEGINNER', points: 1 };
  resourceDraft: Omit<ResourcePayload, 'skillId'> & { skillId: number | null } = { title: '', description: '', skillId: null, resourceType: 'Article', difficultyLevel: 'Beginner', verifiedUrl: '', active: true };

  constructor(
    private readonly adminService: AdminLearningService,
    private readonly skillService: SkillService,
    private readonly frameworkService: FrameworkService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.currentUsername = this.authService.currentUser?.username ?? '';
    this.skillService.getSkills().subscribe({ next: data => this.skills = data, error: () => this.error = 'Could not load the skill catalog.' });
    this.frameworkService.getFrameworks().subscribe({ next: data => this.frameworks = data, error: () => this.error = 'Could not load frameworks.' });
    this.loadAssessments();
    this.loadUsers();
    this.loadResources();
  }

  loadAssessments(): void {
    this.adminService.getAssessments().subscribe({ next: data => {
      this.assessments = data;
      if (this.selectedAssessmentId === null && data.length) this.selectedAssessmentId = data[0].id;
      if (this.selectedAssessmentId !== null) this.loadQuestions();
    }, error: () => this.error = 'Could not load the assessment bank.' });
  }

  createAssessment(): void {
    if (this.assessmentDraft.skillId === null) return;
    this.saving = true;
    this.clearMessages();
    this.adminService.createAssessment({ ...this.assessmentDraft, skillId: this.assessmentDraft.skillId }).subscribe({
      next: created => {
        this.saving = false;
        this.success = 'Assessment created. Add at least one question before learners can take it.';
        this.selectedAssessmentId = created.id;
        this.assessmentDraft = { title: '', instructions: '', skillId: null, passPercentage: 70, maxAttempts: 3 };
        this.loadAssessments();
      },
      error: () => { this.saving = false; this.error = 'Could not create the assessment. Check the skill and thresholds.'; }
    });
  }

  addQuestion(): void {
    if (this.selectedAssessmentId === null || this.questionDraft.options.some(option => !option.trim())) return;
    this.saving = true;
    this.clearMessages();
    this.adminService.createQuestion(this.selectedAssessmentId, this.questionDraft).subscribe({
      next: () => {
        this.saving = false;
        this.success = 'Question added.';
        this.questionDraft = { prompt: '', options: ['', '', '', ''], correctOptionIndex: 0, difficulty: 'BEGINNER', points: 1 };
        this.loadAssessments();
      },
      error: () => { this.saving = false; this.error = 'Could not add this question.'; }
    });
  }

  loadQuestions(): void {
    if (this.selectedAssessmentId === null) { this.questions = []; return; }
    this.adminService.getQuestions(this.selectedAssessmentId).subscribe({
      next: data => this.questions = data,
      error: () => this.error = 'Could not load questions for this assessment.'
    });
  }

  toggleAssessment(assessment: LearnerAssessment): void {
    const skill = this.skills.find(item => item.id === assessment.skillId);
    if (!assessment.id || !skill) return;
    this.adminService.updateAssessment(assessment.id, {
      title: assessment.title,
      instructions: assessment.instructions,
      skillId: assessment.skillId,
      passPercentage: assessment.passPercentage,
      maxAttempts: assessment.maxAttempts,
      active: !assessment.active
    }).subscribe({
      next: updated => {
        this.assessments = this.assessments.map(item => item.id === updated.id ? updated : item);
        this.success = `Assessment ${updated.active ? 'activated' : 'deactivated'}.`;
      },
      error: () => this.error = 'Could not update assessment status.'
    });
  }

  loadUsers(): void {
    this.adminService.getUsers(this.userPage).subscribe({
      next: page => { this.users = page.content; this.userTotalPages = page.totalPages; },
      error: () => this.error = 'Could not load user accounts.'
    });
  }

  setUserStatus(user: AdminUser): void {
    this.saving = true;
    this.clearMessages();
    this.adminService.setUserEnabled(user.id, !user.enabled).subscribe({
      next: () => { this.saving = false; this.success = `Account ${user.enabled ? 'deactivated' : 'reactivated'}.`; this.loadUsers(); },
      error: () => { this.saving = false; this.error = 'Could not change account status. Self-lockout and last-admin deactivation are blocked.'; }
    });
  }

  changeUserPage(delta: number): void { this.userPage += delta; this.loadUsers(); }

  loadAnalytics(): void {
    if (this.analyticsFrameworkId === null) { this.analytics = null; return; }
    this.adminService.getAnalytics(this.analyticsFrameworkId, this.analyticsPage, this.skillFilter).subscribe({
      next: data => this.analytics = data,
      error: () => this.error = 'Could not load organization skill-gap analytics.'
    });
  }

  changeAnalyticsPage(delta: number): void { this.analyticsPage += delta; this.loadAnalytics(); }

  loadResources(): void {
    this.adminService.getResources().subscribe({
      next: data => this.resources = data,
      error: () => this.error = 'Could not load learning resources.'
    });
  }

  toggleResource(resource: AdminResource): void {
    const payload: ResourcePayload = {
      title: resource.title,
      description: resource.description,
      skillId: resource.skillId,
      resourceType: resource.resourceType,
      difficultyLevel: resource.difficultyLevel,
      verifiedUrl: resource.verifiedUrl ?? '',
      active: !resource.active
    };
    this.adminService.updateResource(resource.id, payload).subscribe({
      next: updated => {
        this.resources = this.resources.map(item => item.id === updated.id ? updated : item);
        this.success = `Resource ${updated.active ? 'activated' : 'deactivated'}.`;
      },
      error: () => this.error = 'Could not update resource status.'
    });
  }

  createResource(): void {
    if (!this.resourceDraft.skillId) return;
    this.saving = true;
    this.clearMessages();
    const payload: ResourcePayload = {
      ...this.resourceDraft,
      skillId: this.resourceDraft.skillId
    };
    this.adminService.createResource(payload).subscribe({
      next: resource => {
        this.resources = [resource, ...this.resources];
        this.resourceDraft = { title: '', description: '', skillId: null, resourceType: 'Article', difficultyLevel: 'Beginner', verifiedUrl: '', active: true };
        this.saving = false;
        this.success = 'Learning resource saved.';
      },
      error: () => { this.saving = false; this.error = 'Could not save the resource. Only absolute HTTP or HTTPS URLs are accepted.'; }
    });
  }

  back(): void { void this.router.navigateByUrl('/hr'); }
  private clearMessages(): void { this.error = ''; this.success = ''; }
}
