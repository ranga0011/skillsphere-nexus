import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FrameworkService } from './framework.service';
import { SkillService } from './skill.service';
import { Skill } from './skill';
import { CompetencyFramework, Course, ScorecardEmail } from './feature-models';
import { CourseService } from './course.service';
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
          <p class="eyebrow">SKILLSPHERE NEXUS - ADMINISTRATION</p>
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
        <button type="button" [class.active]="tab === 'emails'" (click)="tab = 'emails'; loadScorecardEmails()">Dispatched Scorecards</button>
      </nav>
      <p class="error-message" role="alert" *ngIf="error">{{ error }}</p>
      <p class="success-message" role="status" *ngIf="success">{{ success }}</p>

      <section class="panel" *ngIf="tab === 'assessments'">
        <h2>Create an assessment</h2>
        <form #assessmentForm="ngForm" (ngSubmit)="createAssessment()">
          
          <div class="ai-course-generator-box">
            <div class="ai-box-badge">AI COURSE ASSIGNMENT INTELLIGENCE</div>
            <div class="form-grid">
              <label>Link Course (For Automated AI Question Generation)
                <select name="aiCourseSelect" [(ngModel)]="aiCourseId" (ngModelChange)="onAiCourseChange()">
                  <option [ngValue]="null">-- Select a Course to Generate Relevant Questions --</option>
                  <option *ngFor="let course of courses" [ngValue]="course.id">{{ course.title }} ({{ course.category }})</option>
                </select>
              </label>
              <label>Number of AI Questions
                <input type="number" name="aiCount" min="1" max="15" [(ngModel)]="aiQuestionCount" />
              </label>
              <div class="ai-gen-btn-wrap">
                <button type="button" class="secondary-btn ai-btn" (click)="generateAiQuestions()" [disabled]="!aiCourseId || generatingAiQuestions">
                  {{ generatingAiQuestions ? 'Generating Questions...' : ' AI Generate Course Questions' }}
                </button>
              </div>
            </div>
            <p class="ai-hint-txt" *ngIf="aiGeneratedQuestions.length > 0">
              [OK] {{ aiGeneratedQuestions.length }} questions ready. Clicking 'Create assessment' will publish them all immediately.
            </p>
          </div>
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
        
        <div class="ai-review-container" *ngIf="aiGeneratedQuestions.length > 0">
          <h3>AI Generated Questions for Assessment ({{ aiGeneratedQuestions.length }})</h3>
          <div *ngFor="let q of aiGeneratedQuestions; let qIdx = index" class="ai-q-card">
            <div class="ai-q-head">
              <strong>Q{{ qIdx + 1 }}. {{ q.prompt }}</strong>
              <span class="status-pill">{{ q.difficulty }}</span>
              <button type="button" class="search-clear-btn" (click)="removeAiQuestion(qIdx)">&times;</button>
            </div>
            <div class="ai-q-opts">
              <span *ngFor="let opt of q.options; let optIdx = index" class="ai-opt-chip" [class.correct]="optIdx === q.correctOptionIndex">
                {{ optIdx === q.correctOptionIndex ? '[OK] ' : '' }}{{ opt }}
              </span>
            </div>
          </div>
        </div>
        <h2>Assessment bank</h2>
        <div class="table-search-bar" *ngIf="assessments.length > 0">
          <span class="search-input-icon"><svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2" class="search-svg"><circle cx="8.5" cy="8.5" r="5.5"></circle><line x1="12.5" y1="12.5" x2="17" y2="17"></line></svg></span>
          <input
            type="search"
            class="table-search-input"
            [(ngModel)]="assessmentSearch"
            placeholder="Search assessments..."
          />
          <button *ngIf="assessmentSearch" class="search-clear-btn" (click)="assessmentSearch = ''">&times;</button>
        </div>
        <p *ngIf="assessments.length === 0">No assessments have been configured yet.</p>
        <article class="list-card" *ngFor="let assessment of filteredAssessments">
          <div>
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
              <strong>{{ assessment.title }}</strong>
              <span class="course-assignment-badge" *ngIf="assessment.courseTitle">Course: {{ assessment.courseTitle }}</span>
            </div>
            <span>{{ assessment.skillName }} &bull; {{ assessment.questionCount }} questions &bull; Pass requirement {{ assessment.passPercentage }}% &bull; {{ assessment.attemptsRemaining }} attempts shown to learners</span>
            <small>{{ assessment.instructions }}</small>
            <div style="margin-top: 6px;">
              <button class="secondary-btn btn-sm" type="button" (click)="toggleViewQuestions(assessment.id)">
                {{ expandedAssessmentId === assessment.id ? 'Hide Questions' : 'View Questions (' + assessment.questionCount + ')' }}
              </button>
            </div>
            <div class="expanded-questions-box" *ngIf="expandedAssessmentId === assessment.id">
              <p *ngIf="loadingQuestions" style="color: #64748b; font-size: 12px; margin: 4px 0;">Loading questions...</p>
              <div *ngIf="!loadingQuestions && expandedQuestions.length === 0" style="color: #64748b; font-size: 12px; margin-top: 4px;">
                No questions configured for this assessment.
              </div>
              <div *ngFor="let q of expandedQuestions; let qi = index" class="preview-q-row">
                <div class="preview-q-head">
                  <strong>Q{{ qi + 1 }}. {{ q.prompt }}</strong>
                  <span class="preview-q-diff">{{ q.difficulty }} ({{ q.points }} pt{{ q.points > 1 ? 's' : '' }})</span>
                </div>
                <div class="preview-q-opts">
                  <span *ngFor="let opt of q.options; let oi = index" class="preview-opt-item">
                    {{ ['A','B','C','D','E'][oi] || oi + 1 }}. {{ opt }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div class="status-actions">
            <span class="status-pill" [class.success]="assessment.active" [class.warning]="!assessment.active">{{ assessment.active ? 'ACTIVE' : 'INACTIVE' }}</span>
            <button class="secondary-btn" type="button" (click)="toggleAssessment(assessment)">{{ assessment.active ? 'Deactivate' : 'Activate' }}</button>
          </div>
        </article>
      </section>

      <section class="panel" *ngIf="tab === 'users'">
        <div class="panel-heading"><div><h2>User accounts</h2><p>Deactivated accounts are blocked at login and on every authenticated API request.</p></div>
          <button class="secondary-btn" type="button" (click)="loadUsers()">Refresh</button></div>
                <div class="table-search-bar" *ngIf="users.length > 0">
          <span class="search-input-icon"><svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2" class="search-svg"><circle cx="8.5" cy="8.5" r="5.5"></circle><line x1="12.5" y1="12.5" x2="17" y2="17"></line></svg></span>
          <input
            type="search"
            class="table-search-input"
            [(ngModel)]="userSearch"
            placeholder="Search users by username, role..."
          />
          <button *ngIf="userSearch" class="search-clear-btn" (click)="userSearch = ''">&times;</button>
        </div>
        <div class="table-wrap"><table><thead><tr><th>USERNAME</th><th>ROLE</th><th>PROFILE</th><th>STATUS</th><th>ACTION</th></tr></thead>
          <tbody><tr *ngFor="let user of filteredUsers">
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
          <div class="pagination"><button type="button" class="secondary-btn" (click)="changeAnalyticsPage(-1)" [disabled]="analyticsPage === 0">Previous</button><span>Page {{ analyticsPage + 1 }} - {{ report.totalSkillEntries }} skills in filter</span><button type="button" class="secondary-btn" (click)="changeAnalyticsPage(1)" [disabled]="(analyticsPage + 1) * report.size >= report.totalSkillEntries">Next</button></div>
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
        <div class="table-search-bar" *ngIf="resources.length > 0">
          <span class="search-input-icon"><svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2" class="search-svg"><circle cx="8.5" cy="8.5" r="5.5"></circle><line x1="12.5" y1="12.5" x2="17" y2="17"></line></svg></span>
          <input
            type="search"
            class="table-search-input"
            [(ngModel)]="resourceSearch"
            placeholder="Search learning resources..."
          />
          <button *ngIf="resourceSearch" class="search-clear-btn" (click)="resourceSearch = ''">&times;</button>
        </div>
        <article class="list-card" *ngFor="let resource of filteredResources">
          <div><strong>{{ resource.title }} - {{ resource.skillName }}</strong><span>{{ resource.resourceType }} - {{ resource.difficultyLevel }} - {{ resource.active ? 'Active' : 'Inactive' }}</span><small>{{ resource.description }}</small><a *ngIf="resource.verifiedUrl" [href]="resource.verifiedUrl" target="_blank" rel="noopener noreferrer">{{ resource.verifiedUrl }}</a></div>
          <button class="secondary-btn" type="button" (click)="toggleResource(resource)">{{ resource.active ? 'Deactivate' : 'Activate' }}</button>
        </article>
        <p *ngIf="resources.length === 0">No resources configured yet; learners will be told when a gap has no matching resource.</p>
      </section>
    
      <section class="panel" *ngIf="tab === 'emails'">
        <div class="panel-heading">
          <div>
            <h2>Dispatched Scorecard Emails</h2>
            <p>Official grade notifications sent automatically to students and employees upon submitting assessments.</p>
          </div>
          <button class="secondary-btn" type="button" (click)="loadScorecardEmails()">Refresh Emails</button>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>RECIPIENT</th>
                <th>ASSESSMENT</th>
                <th>MARKS</th>
                <th>PERCENTAGE</th>
                <th>STATUS</th>
                <th>SENT DATE</th>
                <th>ACTION</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let email of scorecardEmails">
                <td><strong>{{ email.recipientName || 'Learner' }}</strong><br/><small>{{ email.recipientEmail }}</small></td>
                <td>{{ email.assessmentTitle }}</td>
                <td>{{ email.scorePoints }} / {{ email.maxPoints }}</td>
                <td>{{ email.percentage }}%</td>
                <td><span class="status-pill" [class.success]="email.passed" [class.warning]="!email.passed">{{ email.passed ? 'PASSED' : 'NOT PASSED' }}</span></td>
                <td>{{ email.sentAt | date:'short' }}</td>
                <td><button class="secondary-btn btn-sm" type="button" (click)="activeEmailModal = email">View Email</button></td>
              </tr>
              <tr *ngIf="scorecardEmails.length === 0">
                <td colspan="7">No scorecard emails dispatched yet. Complete assessments in the student or employee portal to see email logs.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- Scorecard Email Preview Modal -->
      <div class="modal-backdrop" *ngIf="activeEmailModal" (click)="activeEmailModal = null">
        <div class="modal-content scorecard-email-modal" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <div>
              <span class="modal-eyebrow">DISPATCHED SCORECARD EMAIL AUDIT</span>
              <h3>{{ activeEmailModal.subject }}</h3>
              <p>Sent to: <strong>{{ activeEmailModal.recipientEmail }}</strong> &bull; {{ activeEmailModal.sentAt | date:'medium' }}</p>
            </div>
            <button class="modal-close-btn" (click)="activeEmailModal = null">&times;</button>
          </div>
          <div class="modal-body">
            <pre class="email-raw-body">{{ activeEmailModal.bodyText }}</pre>
          </div>
          <div class="modal-footer">
            <button class="primary-btn" (click)="activeEmailModal = null">Close</button>
          </div>
        </div>
      </div>
    </main>
  `,
  styleUrl: './admin-learning.component.scss'
})
export class AdminLearningComponent implements OnInit {
  tab: 'assessments' | 'users' | 'analytics' | 'resources' | 'emails' = 'assessments';
  skills: Skill[] = [];
  frameworks: CompetencyFramework[] = [];
  assessments: LearnerAssessment[] = [];
  questions: Array<{ id: number; prompt: string; difficulty: string }> = [];
  users: AdminUser[] = [];
  resources: AdminResource[] = [];
  analytics: GapAnalytics | null = null;
  analyticsFrameworkId: number | null = null;
  analyticsPage = 0;
  skillFilter = '';
  userPage = 0;
  userTotalPages = 0;
  currentUsername = '';
  saving = false;
  error = '';
  userSearch = '';
  assessmentSearch = '';
  resourceSearch = '';

  get filteredUsers(): AdminUser[] {
    const q = this.userSearch.trim().toLowerCase();
    if (!q) return this.users;
    return this.users.filter(u =>
      (u.username && u.username.toLowerCase().includes(q)) ||
      (u.role && u.role.toLowerCase().includes(q))
    );
  }

  get filteredAssessments(): LearnerAssessment[] {
    const q = this.assessmentSearch.trim().toLowerCase();
    if (!q) return this.assessments;
    return this.assessments.filter(a =>
      (a.title && a.title.toLowerCase().includes(q)) ||
      (a.skillName && a.skillName.toLowerCase().includes(q)) ||
      (a.instructions && a.instructions.toLowerCase().includes(q))
    );
  }

  get filteredResources(): AdminResource[] {
    const q = this.resourceSearch.trim().toLowerCase();
    if (!q) return this.resources;
    return this.resources.filter(r =>
      (r.title && r.title.toLowerCase().includes(q)) ||
      (r.skillName && r.skillName.toLowerCase().includes(q)) ||
      (r.description && r.description.toLowerCase().includes(q))
    );
  }
  success = '';
    courses: Course[] = [];
  aiCourseId: number | null = null;
  aiQuestionCount = 5;
  generatingAiQuestions = false;
  aiGeneratedQuestions: any[] = [];
  scorecardEmails: ScorecardEmail[] = [];
  activeEmailModal: ScorecardEmail | null = null;
  assessmentDraft = { title: '', instructions: '', skillId: null as number | null, passPercentage: 70, maxAttempts: 3 };
  expandedAssessmentId: number | null = null;
  expandedQuestions: any[] = [];
  loadingQuestions = false;
  resourceDraft: Omit<ResourcePayload, 'skillId'> & { skillId: number | null } = { title: '', description: '', skillId: null, resourceType: 'Article', difficultyLevel: 'Beginner', verifiedUrl: '', active: true };

  constructor(
    private readonly adminService: AdminLearningService,
    private readonly skillService: SkillService,
    private readonly frameworkService: FrameworkService,
    private readonly authService: AuthService,
    private readonly courseService: CourseService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.currentUsername = this.authService.currentUser?.username ?? '';
    this.skillService.getSkills().subscribe({ next: data => this.skills = data, error: () => this.error = 'Could not load the skill catalog.' });
    this.frameworkService.getFrameworks().subscribe({ next: data => this.frameworks = data, error: () => this.error = 'Could not load frameworks.' });
    this.loadAssessments();
    this.loadUsers();
    this.loadResources();
    this.loadCourses();
    this.loadScorecardEmails();
  }
  loadCourses(): void {
    this.courseService.getAllCourses().subscribe({
      next: (courses) => this.courses = courses,
      error: () => {}
    });
  }

  loadScorecardEmails(): void {
    this.adminService.getScorecardEmails().subscribe({
      next: (emails) => this.scorecardEmails = emails,
      error: () => {}
    });
  }

  onAiCourseChange(): void {
    if (!this.aiCourseId) return;
    const course = this.courses.find(c => c.id === this.aiCourseId);
    if (!course) return;

    this.assessmentDraft.title = course.title + ' - Comprehensive Assessment';
    this.assessmentDraft.instructions = 'Complete all questions to verify your mastery of ' + course.title + '. Minimum ' + this.assessmentDraft.passPercentage + '% required to pass.';

    const cLower = (course.title + ' ' + (course.category || '')).toLowerCase();
    const matched = this.skills.find(s => {
      const sLower = s.name.toLowerCase();
      return cLower.includes(sLower) || sLower.split(' ').some(w => w.length > 3 && cLower.includes(w));
    });
    if (matched) {
      this.assessmentDraft.skillId = matched.id ?? null;
    }
    this.generateAiQuestions();
  }

  generateAiQuestions(): void {
    if (!this.aiCourseId) return;
    this.generatingAiQuestions = true;
    this.adminService.aiGenerateQuestions(this.aiCourseId, this.aiQuestionCount || 5).subscribe({
      next: (questions) => {
        this.aiGeneratedQuestions = questions;
        this.generatingAiQuestions = false;
        this.success = 'Generated ' + questions.length + ' AI questions for this course!';
      },
      error: () => {
        this.generatingAiQuestions = false;
        this.error = 'Could not generate AI questions. Please try again.';
      }
    });
  }

  removeAiQuestion(index: number): void {
    this.aiGeneratedQuestions.splice(index, 1);
  }


  loadAssessments(): void {
    this.adminService.getAssessments().subscribe({
      next: data => {
        this.assessments = data;
        if (this.expandedAssessmentId !== null && !data.some(a => a.id === this.expandedAssessmentId)) {
          this.expandedAssessmentId = null;
          this.expandedQuestions = [];
        }
      },
      error: () => this.error = 'Could not load the assessment bank.'
    });
  }

  createAssessment(): void {
    if (this.assessmentDraft.skillId === null) return;
    this.saving = true;
    this.clearMessages();

    if (this.aiGeneratedQuestions.length === 0) {
      this.saving = false;
      this.error = 'Please select a course to generate AI questions before creating the assessment.';
      return;
    }

    const course = this.courses.find(c => c.id === this.aiCourseId);
    const payload = {
      title: this.assessmentDraft.title.trim(),
      instructions: this.assessmentDraft.instructions.trim(),
      skillId: this.assessmentDraft.skillId,
      courseId: this.aiCourseId || undefined,
      courseTitle: course ? course.title : undefined,
      passPercentage: this.assessmentDraft.passPercentage,
      maxAttempts: this.assessmentDraft.maxAttempts,
      active: true,
      questions: this.aiGeneratedQuestions
    };

    this.adminService.createAssessmentWithQuestions(payload).subscribe({
      next: () => {
        this.saving = false;
        this.success = 'Assessment published with ' + this.aiGeneratedQuestions.length + ' AI-generated questions and made available to students!';
        this.aiGeneratedQuestions = [];
        this.aiCourseId = null;
        this.assessmentDraft = { title: '', instructions: '', skillId: null, passPercentage: 70, maxAttempts: 3 };
        this.loadAssessments();
      },
      error: () => {
        this.saving = false;
        this.error = 'Could not create assessment with AI questions.';
      }
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

  toggleViewQuestions(assessmentId: number): void {
    if (this.expandedAssessmentId === assessmentId) {
      this.expandedAssessmentId = null;
      this.expandedQuestions = [];
      return;
    }
    this.expandedAssessmentId = assessmentId;
    this.loadingQuestions = true;
    this.adminService.getQuestions(assessmentId).subscribe({
      next: data => {
        this.expandedQuestions = data;
        this.loadingQuestions = false;
      },
      error: () => {
        this.expandedQuestions = [];
        this.loadingQuestions = false;
        this.error = 'Could not load questions for this assessment.';
      }
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
