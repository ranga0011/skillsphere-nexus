import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Assessment, CompetencyFramework, CompetencyGapReport, EmployeeSkillAssignment, IssuedCertificate, Student, ScorecardEmail } from './feature-models';
import { FrameworkService } from './framework.service';
import { IssuedCertificateService } from './issued-certificate.service';
import { SkillService } from './skill.service';
import { Skill } from './skill';
import { StudentService } from './student.service';
import { LearningService, LearnerAssessment, LearnerAttemptResult, LearningResourceRecommendation, StartedAssessment } from './learning.service';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="student-shell">
      <header class="student-header">
        <div>
          <p class="eyebrow">SKILLSPHERE NEXUS - STUDENT</p>
          <h1>Your learning workspace</h1>
          <p>Track verified skills, assessments, competency gaps, and certificates.</p>
        </div>
        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <button *ngIf="authService.isHrAdmin" class="secondary-btn" type="button" (click)="backToHr()">Back to HR Workspace</button>
          <button class="secondary-btn" type="button" (click)="refresh()">Refresh</button>
        </div>
      </header>

      <div *ngIf="authService.isHrAdmin" class="hr-preview-banner">
        <strong>HR Administrator Preview Mode:</strong> You are previewing the Student Portal. Assignments and tests visible here are exactly what students see.
      </div>

      <p class="error-message" role="alert" *ngIf="errorMessage">{{ errorMessage }}</p>
      <p class="success-message" role="status" *ngIf="successMessage">{{ successMessage }}</p>

      <section class="panel">
        <h2>Student profile</h2>
        <p *ngIf="profileLoading">Loading your profile...</p>
        <form *ngIf="profile" #profileForm="ngForm" (ngSubmit)="saveProfile()">
          <div class="form-grid">
            <label>Full name
              <input name="name" [(ngModel)]="profile.name" required maxlength="100" />
            </label>
            <label>Email
              <input name="email" [(ngModel)]="profile.email" required email maxlength="150" />
            </label>
            <label>Program
              <input name="program" [(ngModel)]="profile.program" required maxlength="150" />
            </label>
          </div>
          <button class="primary-btn" type="submit" [disabled]="profileForm.invalid || profileSaving">
            {{ profileSaving ? 'Saving...' : 'Update profile' }}
          </button>
        </form>
      </section>

      <section class="panel">
        <div class="panel-heading">
          <div>
            <h2>Your skills</h2>
            <p>Self-reported skills stay pending until an authorized assessor verifies them.</p>
          </div>
        </div>
        <p>Adding a skill requests assessment; certificates are issued only after a passing result is verified.</p>
        <form class="inline-form" #skillForm="ngForm" (ngSubmit)="requestSkill()">
          <label>Skill
            <select name="skillId" [(ngModel)]="skillId" required>
              <option [ngValue]="null" disabled>Select a catalog skill</option>
              <option *ngFor="let skill of skills" [ngValue]="skill.id">{{ skill.name }}</option>
            </select>
          </label>
          <label>Self-reported proficiency (1-10)
            <input type="number" name="proficiency" min="1" max="10" [(ngModel)]="proficiency" required />
          </label>
          <button class="primary-btn" type="submit" [disabled]="skillForm.invalid || skillSaving">
            {{ skillSaving ? 'Requesting...' : 'Request skill' }}
          </button>
        </form>
        <p *ngIf="skillsLoading">Loading your skills...</p>
        <div class="table-search-bar" *ngIf="!skillsLoading && assignments.length > 0">
          <span class="search-input-icon"><svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2" class="search-svg"><circle cx="8.5" cy="8.5" r="5.5"></circle><line x1="12.5" y1="12.5" x2="17" y2="17"></line></svg></span>
          <input
            type="search"
            class="table-search-input"
            [(ngModel)]="skillSearch"
            placeholder="Search your skills..."
          />
          <button *ngIf="skillSearch" class="search-clear-btn" (click)="skillSearch = ''">&times;</button>
        </div>
        <div class="table-wrap" *ngIf="!skillsLoading">
          <table>
            <thead><tr><th>SKILL</th><th>SELF-REPORTED LEVEL</th><th>VERIFICATION</th></tr></thead>
            <tbody>
              <tr *ngFor="let item of filteredAssignments">
                <td>{{ item.skill?.name || 'Skill' }}</td>
                <td>{{ item.proficiency }}/10</td>
                <td><span [class]="badgeClass(item.verificationStatus)">{{ item.verificationStatus }}</span></td>
              </tr>
              <tr *ngIf="assignments.length === 0"><td colspan="3">No skills added yet. Browse the catalog above to request one.</td></tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <h2>Skill-gap analysis</h2>
        <p>Readiness is a skill-level coverage measure, not a hiring probability.</p>
        <label class="framework-select">Target role or framework
          <select name="framework" [(ngModel)]="frameworkId" (ngModelChange)="loadGapReport()" [disabled]="frameworks.length === 0">
            <option [ngValue]="null">Select a framework</option>
            <option *ngFor="let framework of frameworks" [ngValue]="framework.id">{{ framework.roleTitle }} - {{ framework.name }}</option>
          </select>
        </label>
        <p *ngIf="gapLoading">Calculating your report...</p>
        <ng-container *ngIf="gapReport as report">
          <div class="readiness">
            <strong>{{ report.readinessPercentage | number:'1.0-1' }}%</strong>
            <span>verified readiness for {{ report.roleTitle }}</span>
          </div>
          <div class="summary-grid">
            <article><strong>{{ report.requiredSkills }}</strong><span>required skills</span></article>
            <article><strong>{{ report.satisfiedRequirements }}</strong><span>requirements met</span></article>
            <article><strong>{{ report.missingSkills }}</strong><span>missing</span></article>
            <article><strong>{{ report.unverifiedSkills }}</strong><span>awaiting verification</span></article>
          </div>
          <p class="formula">{{ report.readinessFormula }}</p>
          <div class="table-wrap">
            <table>
              <thead><tr><th>SKILL</th><th>VERIFIED LEVEL</th><th>REQUIRED</th><th>GAP</th><th>STATUS</th><th>NEXT STEP</th></tr></thead>
              <tbody>
                <tr *ngFor="let gap of report.skills">
                  <td>{{ gap.skillName }}</td>
                  <td>{{ gap.employeeProficiency ?? '-' }}</td>
                  <td>{{ gap.requiredProficiency }}/10</td>
                  <td>{{ gap.gap }}</td>
                  <td>{{ gap.status }}</td>
                  <td>{{ gap.recommendation }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </ng-container>
        <p *ngIf="!gapLoading && frameworkId === null">Choose a framework to see requirements for that target role.</p>
      </section>

      <section class="panel">
        <h2>Available assessments & Course Assignments</h2>
        <p>Assignments created by HR for your courses appear here. Questions are scored on the server and results are automatically emailed to you.</p>
        <p *ngIf="availableAssessmentsLoading">Loading available assessments...</p>
        <div class="table-search-bar" *ngIf="!availableAssessmentsLoading && availableAssessments.length > 0">
          <span class="search-input-icon"><svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2" class="search-svg"><circle cx="8.5" cy="8.5" r="5.5"></circle><line x1="12.5" y1="12.5" x2="17" y2="17"></line></svg></span>
          <input
            type="search"
            class="table-search-input"
            [(ngModel)]="assessmentSearch"
            placeholder="Search available assessments..."
          />
          <button *ngIf="assessmentSearch" class="search-clear-btn" (click)="assessmentSearch = ''">&times;</button>
        </div>
        <div class="assessment-card" *ngFor="let assessment of filteredAvailableAssessments">
          <div>
            <span class="course-assignment-badge" *ngIf="assessment.courseTitle">COURSE ASSIGNMENT: {{ assessment.courseTitle }}</span>
            <h3>{{ assessment.title }} - {{ assessment.skillName }}</h3>
            <p>{{ assessment.instructions }}</p>
            <small>{{ assessment.questionCount }} questions &bull; Passing score {{ assessment.passPercentage }}% &bull; {{ assessment.attemptsRemaining }} attempt(s) remaining</small>
          </div>
          <button class="primary-btn" type="button" (click)="startAssessment(assessment)" [disabled]="startingAssessmentId === assessment.id">
            {{ startingAssessmentId === assessment.id ? 'Starting...' : 'Start Assessment' }}
          </button>
        </div>
        <p *ngIf="!availableAssessmentsLoading && availableAssessments.length === 0">No active assessments are available for your profile right now.</p>
        <div class="attempt-panel" *ngIf="activeAttempt as attempt">
          <div class="attempt-header">
            <span class="course-assignment-badge" *ngIf="attempt.assessment.courseTitle">COURSE: {{ attempt.assessment.courseTitle }}</span>
            <h3>{{ attempt.assessment.title }} - Attempt {{ attempt.attemptNumber }}</h3>
            <p>{{ attempt.assessment.instructions }}</p>
          </div>
          <fieldset *ngFor="let question of attempt.questions; let questionNumber = index">
            <legend>{{ questionNumber + 1 }}. {{ question.prompt }} ({{ question.points }} point(s), {{ question.difficulty | lowercase }})</legend>
            <label class="answer-option" *ngFor="let option of question.options; let optionIndex = index">
              <input type="radio" [name]="'question-' + question.id" [value]="optionIndex" [(ngModel)]="question.selectedOptionIndex" />
              {{ option }}
            </label>
          </fieldset>
          <div class="attempt-actions">
            <button class="primary-btn" type="button" (click)="submitAssessment()" [disabled]="submittingAssessment || !allQuestionsAnswered(attempt)">
              {{ submittingAssessment ? 'Submitting & Scoring...' : 'Submit Answers & Calculate Grade' }}
            </button>
          </div>
        </div>
      </section>

      <!-- Latest Attempt Result & Automated Scorecard Email Banner -->
      <section class="panel scorecard-result-panel" *ngIf="latestAttemptResult as result">
        <h2>Latest Assessment Evaluation & Scorecard</h2>
        <div class="result-badge-bar">
          <span class="status-pill" [class.success]="result.passed" [class.warning]="!result.passed">
            {{ result.passed ? 'PASSED & CLEARED' : 'NOT PASSED' }}
          </span>
          <span class="score-pill">Marks: <strong>{{ result.scorePoints }} / {{ result.maxPoints }}</strong> ({{ result.percentage }}%)</span>
          <span class="threshold-pill">Pass requirement: {{ result.passPercentage }}%</span>
        </div>
        <p class="success-message" *ngIf="result.passed">Congratulations! You cleared this assignment with {{ result.percentage }}%.</p>
        <p class="error-message" *ngIf="!result.passed">Score: {{ result.percentage }}% (required {{ result.passPercentage }}%). You may use remaining attempts.</p>

        <!-- Email Notification Notice -->
        <div class="email-sent-banner">
          <div class="email-banner-icon">&#9993;</div>
          <div class="email-banner-text">
            <strong>Official Scorecard Email Dispatched!</strong>
            <p>An official scorecard email detailing your score ({{ result.scorePoints }}/{{ result.maxPoints }} Marks - {{ result.percentage }}%) has been sent to <u>{{ result.emailRecipient || profile?.email || 'your email' }}</u>.</p>
          </div>
          <button class="secondary-btn btn-sm" type="button" (click)="viewEmailPreview(result)">
            Preview Sent Email
          </button>
        </div>

        <div class="result-actions" style="margin-top: 1rem; display: flex; gap: 0.75rem;">
          <button class="primary-btn" type="button" *ngIf="result.certificate" (click)="download(result.certificate)">
            Download Certificate {{ result.certificate.certificateNumber }} PDF
          </button>
        </div>
      </section>

      <section class="panel">
        <h2>Online assessment attempts history</h2>
        <div class="table-wrap">
          <table><thead><tr><th>ASSESSMENT</th><th>ATTEMPT</th><th>RESULT</th><th>SUBMITTED</th><th>STATUS</th></tr></thead>
            <tbody><tr *ngFor="let item of attemptHistory">
              <td>{{ item.assessmentTitle }}</td><td>{{ item.attemptNumber }}</td>
              <td>{{ item.scorePoints }}/{{ item.maxPoints }} ({{ item.percentage }}%)</td>
              <td>{{ item.submittedAt }}</td><td><span class="status-pill" [class.success]="item.passed" [class.warning]="!item.passed">{{ item.passed ? 'PASSED' : 'NOT PASSED' }}</span></td>
            </tr><tr *ngIf="attemptHistory.length === 0"><td colspan="5">No online assessment attempts submitted yet.</td></tr></tbody>
          </table>
        </div>
      </section>

      <!-- Dispatched Scorecards / Email History Section -->
      <section class="panel">
        <div class="panel-heading">
          <div>
            <h2>Dispatched Scorecard Emails</h2>
            <p>Official grade notification emails sent to your registered address upon completing assignments.</p>
          </div>
          <button class="secondary-btn" type="button" (click)="loadMyScorecardEmails()">Refresh Emails</button>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ASSESSMENT</th>
                <th>MARKS / SCORE</th>
                <th>RECIPIENT EMAIL</th>
                <th>RESULT</th>
                <th>SENT DATE</th>
                <th>ACTION</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let em of scorecardEmails">
                <td><strong>{{ em.assessmentTitle }}</strong></td>
                <td>{{ em.scorePoints }}/{{ em.maxPoints }} ({{ em.percentage }}%)</td>
                <td>{{ em.recipientEmail }}</td>
                <td><span class="status-pill" [class.success]="em.passed" [class.warning]="!em.passed">{{ em.passed ? 'PASSED' : 'NOT PASSED' }}</span></td>
                <td>{{ em.sentAt }}</td>
                <td>
                  <button class="secondary-btn btn-sm" type="button" (click)="openEmailModal(em)">View Email</button>
                </td>
              </tr>
              <tr *ngIf="scorecardEmails.length === 0">
                <td colspan="6">No scorecard emails dispatched yet. Complete an assessment above to receive your scorecard email.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <div class="panel-heading">
          <div>
            <h2>Achievement certificates</h2>
            <p>Issued automatically after an authorized assessor verifies a passing result.</p>
          </div>
          <button class="secondary-btn" type="button" (click)="loadCertificates()">Refresh certificates</button>
        </div>
        <p *ngIf="certificatesLoading">Loading certificates...</p>
        <div class="table-search-bar" *ngIf="!certificatesLoading && certificates.length > 0">
          <span class="search-input-icon"><svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2" class="search-svg"><circle cx="8.5" cy="8.5" r="5.5"></circle><line x1="12.5" y1="12.5" x2="17" y2="17"></line></svg></span>
          <input
            type="search"
            class="table-search-input"
            [(ngModel)]="certificateSearch"
            placeholder="Search certificates..."
          />
          <button *ngIf="certificateSearch" class="search-clear-btn" (click)="certificateSearch = ''">&times;</button>
        </div>
        <div class="table-wrap" *ngIf="!certificatesLoading">
          <table>
            <thead><tr><th>SKILL</th><th>CERTIFICATE</th><th>ISSUED</th><th>STATUS</th><th>ACTION</th></tr></thead>
            <tbody>
              <tr *ngFor="let certificate of filteredCertificates">
                <td>{{ certificate.skillName }}<small>{{ certificate.proficiencyLevel }}/10 verified</small></td>
                <td>{{ certificate.certificateNumber }}<small>{{ certificate.issuerName }}</small></td>
                <td>{{ certificate.issueDate }}</td>
                <td>{{ certificate.status }}</td>
                <td class="actions">
                  <button class="primary-btn" type="button" (click)="download(certificate)" [disabled]="downloadingId === certificate.id || certificate.status !== 'ISSUED'">
                    {{ downloadingId === certificate.id ? 'Downloading...' : 'Download PDF' }}
                  </button>
                  <a [href]="verificationUrl(certificate)" target="_blank" rel="noopener">Verify</a>
                </td>
              </tr>
              <tr *ngIf="certificates.length === 0"><td colspan="5">No achievement certificates yet. Verified passing assessments will appear here.</td></tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- Scorecard Email Modal -->
      <div class="modal-backdrop" *ngIf="activeEmailModal" (click)="activeEmailModal = null">
        <div class="modal-content scorecard-email-modal" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <div>
              <span class="modal-eyebrow">DISPATCHED SCORECARD EMAIL NOTIFICATION</span>
              <h3>{{ activeEmailModal.subject }}</h3>
              <p>Sent to: <strong>{{ activeEmailModal.recipientEmail }}</strong> ({{ activeEmailModal.recipientName || 'Learner' }}) &bull; {{ activeEmailModal.sentAt }}</p>
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

      <footer><button class="secondary-btn" type="button" (click)="logout()">Sign out</button></footer>
    </main>
  `,
  styleUrl: './student-dashboard.scss'
})
export class StudentDashboardComponent implements OnInit {
  profile: Student | null = null;
  skills: Skill[] = [];
  assignments: EmployeeSkillAssignment[] = [];
  assessments: Assessment[] = [];
  frameworks: CompetencyFramework[] = [];
  gapReport: CompetencyGapReport | null = null;
  certificates: IssuedCertificate[] = [];
  availableAssessments: LearnerAssessment[] = [];
  attemptHistory: LearnerAttemptResult[] = [];
  scorecardEmails: ScorecardEmail[] = [];
  activeAttempt: StartedAssessment | null = null;
  latestAttemptResult: LearnerAttemptResult | null = null;
  activeEmailModal: ScorecardEmail | null = null;
  recommendations: LearningResourceRecommendation[] = [];
  skillId: number | null = null;
  frameworkId: number | null = null;
  proficiency = 5;
  profileLoading = true;
  profileSaving = false;
  skillsLoading = true;
  skillSaving = false;
  assessmentLoading = true;
  gapLoading = false;
  certificatesLoading = true;
  availableAssessmentsLoading = true;
  recommendationsLoading = false;
  submittingAssessment = false;
  startingAssessmentId: number | null = null;
  downloadingId: number | null = null;
  errorMessage = '';
  skillSearch = '';
  assessmentSearch = '';
  certificateSearch = '';

  get filteredAssignments(): EmployeeSkillAssignment[] {
    const q = this.skillSearch.trim().toLowerCase();
    if (!q) return this.assignments;
    return this.assignments.filter(a =>
      (a.skill?.name && a.skill.name.toLowerCase().includes(q)) ||
      (a.verificationStatus && a.verificationStatus.toLowerCase().includes(q))
    );
  }

  get filteredAvailableAssessments(): LearnerAssessment[] {
    const q = this.assessmentSearch.trim().toLowerCase();
    if (!q) return this.availableAssessments;
    return this.availableAssessments.filter(a =>
      (a.title && a.title.toLowerCase().includes(q)) ||
      (a.skillName && a.skillName.toLowerCase().includes(q)) ||
      (a.courseTitle && a.courseTitle.toLowerCase().includes(q)) ||
      (a.instructions && a.instructions.toLowerCase().includes(q))
    );
  }

  get filteredCertificates(): IssuedCertificate[] {
    const q = this.certificateSearch.trim().toLowerCase();
    if (!q) return this.certificates;
    return this.certificates.filter(c =>
      (c.skillName && c.skillName.toLowerCase().includes(q)) ||
      (c.certificateNumber && c.certificateNumber.toLowerCase().includes(q)) ||
      (c.issuerName && c.issuerName.toLowerCase().includes(q)) ||
      (c.status && c.status.toLowerCase().includes(q))
    );
  }
  successMessage = '';

  constructor(
    private readonly studentService: StudentService,
    private readonly skillService: SkillService,
    private readonly frameworkService: FrameworkService,
    private readonly certificateService: IssuedCertificateService,
    private readonly learningService: LearningService,
    public readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.errorMessage = '';
    this.loadProfile();
    this.loadCatalog();
    this.loadFrameworks();
    this.loadCertificates();
    this.loadAvailableAssessments();
    this.loadAttemptHistory();
    this.loadMyScorecardEmails();
  }

  backToHr(): void {
    void this.router.navigateByUrl('/hr');
  }

  saveProfile(): void {
    if (!this.profile) {
      return;
    }
    this.profileSaving = true;
    this.studentService.updateMyProfile(this.profile).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileSaving = false;
        this.successMessage = 'Profile updated.';
      },
      error: () => {
        this.profileSaving = false;
        this.errorMessage = 'Could not update your profile.';
      }
    });
  }

  requestSkill(): void {
    if (this.skillId === null) {
      return;
    }
    this.skillSaving = true;
    this.studentService.requestSkill(this.skillId, this.proficiency).subscribe({
      next: () => {
        this.skillSaving = false;
        this.skillId = null;
        this.successMessage = 'Skill request added as pending verification.';
        this.loadMySkills();
      },
      error: () => {
        this.skillSaving = false;
        this.errorMessage = 'Could not request this skill. It may already be on your profile.';
      }
    });
  }

  loadGapReport(): void {
    if (this.frameworkId === null) {
      this.gapReport = null;
      return;
    }
    this.gapLoading = true;
    this.studentService.getMyGapReport(this.frameworkId).subscribe({
      next: (report) => {
        this.gapReport = report;
        this.gapLoading = false;
        this.loadRecommendations();
      },
      error: () => {
        this.gapReport = null;
        this.gapLoading = false;
        this.errorMessage = 'Could not load the selected skill-gap report.';
      }
    });
  }

  loadCertificates(): void {
    this.certificatesLoading = true;
    this.certificateService.getCertificates().subscribe({
      next: (certificates) => {
        this.certificates = certificates;
        this.certificatesLoading = false;
      },
      error: () => {
        this.certificatesLoading = false;
        this.errorMessage = 'Could not load your achievement certificates.';
      }
    });
  }

  download(certificate: IssuedCertificate): void {
    this.downloadingId = certificate.id;
    this.certificateService.saveDownload(certificate).subscribe({
      next: () => {
        this.downloadingId = null;
      },
      error: () => {
        this.downloadingId = null;
        this.errorMessage = 'Could not download this certificate.';
      }
    });
  }

  verificationUrl(certificate: IssuedCertificate): string {
    return `http://localhost:8080/api/certificates/verify/${certificate.verificationToken}`;
  }

  badgeClass(status: string | undefined): string {
    return status === 'VERIFIED' ? 'status-pill success' : 'status-pill warning';
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login');
  }

  private loadProfile(): void {
    this.profileLoading = true;
    this.studentService.getMyProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileLoading = false;
        this.loadMySkills();
        this.loadAssessments();
      },
      error: () => {
        if (this.authService.isHrAdmin) {
          this.studentService.getStudents().subscribe({
            next: (students) => {
              if (students.length > 0) {
                this.profile = students[0];
              }
              this.profileLoading = false;
            },
            error: () => this.profileLoading = false
          });
        } else {
          this.profileLoading = false;
          this.errorMessage = 'Could not load your student profile.';
        }
      }
    });
  }

  private loadCatalog(): void {
    this.skillService.getSkills().subscribe({
      next: (skills) => this.skills = skills,
      error: () => this.errorMessage = 'Could not load the skill catalog.'
    });
  }

  private loadFrameworks(): void {
    this.frameworkService.getFrameworks().subscribe({
      next: (frameworks) => {
        this.frameworks = frameworks;
        if (this.frameworkId === null && frameworks.length > 0) {
          this.frameworkId = frameworks[0].id ?? null;
          this.loadGapReport();
        }
      },
      error: () => this.errorMessage = 'Could not load competency frameworks.'
    });
  }

  private loadMySkills(): void {
    this.skillsLoading = true;
    this.studentService.getMySkills().subscribe({
      next: (assignments) => {
        this.assignments = assignments;
        this.skillsLoading = false;
      },
      error: () => {
        this.skillsLoading = false;
        this.errorMessage = 'Could not load your skills.';
      }
    });
  }

  private loadAssessments(): void {
    this.assessmentLoading = true;
    this.studentService.getMyAssessments().subscribe({
      next: (assessments) => {
        this.assessments = assessments;
        this.assessmentLoading = false;
      },
      error: () => {
        this.assessmentLoading = false;
        this.errorMessage = 'Could not load your assessment history.';
      }
    });
  }

  loadAvailableAssessments(): void {
    this.availableAssessmentsLoading = true;
    this.learningService.getAvailableAssessments().subscribe({
      next: assessments => {
        this.availableAssessments = assessments;
        this.availableAssessmentsLoading = false;
      },
      error: () => {
        this.availableAssessmentsLoading = false;
        this.errorMessage = 'Could not load available assessments.';
      }
    });
  }

  private loadAttemptHistory(): void {
    this.learningService.getAttemptHistory().subscribe({
      next: history => this.attemptHistory = history,
      error: () => this.errorMessage = 'Could not load your assessment attempt history.'
    });
  }

  loadMyScorecardEmails(): void {
    this.learningService.getMyScorecardEmails().subscribe({
      next: (emails) => {
        this.scorecardEmails = emails;
      },
      error: () => {
        // Silently catch if not yet available
      }
    });
  }

  startAssessment(assessment: LearnerAssessment): void {
    this.startingAssessmentId = assessment.id;
    this.errorMessage = '';
    this.learningService.startAssessment(assessment.id).subscribe({
      next: attempt => {
        this.activeAttempt = attempt;
        this.latestAttemptResult = null;
        this.startingAssessmentId = null;
      },
      error: () => {
        this.startingAssessmentId = null;
        this.errorMessage = 'Could not start this assessment. It may be inactive or you may have used the attempt limit.';
        this.loadAvailableAssessments();
      }
    });
  }

  allQuestionsAnswered(attempt: StartedAssessment): boolean {
    return attempt.questions.every(question => question.selectedOptionIndex !== undefined);
  }

  submitAssessment(): void {
    if (!this.activeAttempt || !this.allQuestionsAnswered(this.activeAttempt)) return;
    this.submittingAssessment = true;
    this.errorMessage = '';
    this.learningService.submitAssessment(this.activeAttempt).subscribe({
      next: result => {
        const completedSkillId = this.availableAssessments.find(item => item.id === result.assessmentId)?.skillId;
        this.latestAttemptResult = result;
        this.activeAttempt = null;
        this.submittingAssessment = false;
        this.successMessage = result.passed
          ? `Assignment cleared! Scored ${result.scorePoints}/${result.maxPoints} (${result.percentage}%). Scorecard email sent to ${result.emailRecipient || this.profile?.email}.`
          : `Assignment evaluated. Scored ${result.scorePoints}/${result.maxPoints} (${result.percentage}%). Scorecard email sent to ${result.emailRecipient || this.profile?.email}.`;
        if (result.certificate) {
          this.certificates = [result.certificate,
            ...this.certificates.filter(item => item.id !== result.certificate?.id)];
        }
        this.loadCertificates();
        this.attemptHistory = [result, ...this.attemptHistory.filter(item => item.attemptId !== result.attemptId)];
        if (completedSkillId !== undefined) {
          this.assignments = this.assignments.filter(item => item.skillId !== completedSkillId);
        }
        this.loadMySkills();
        this.loadAttemptHistory();
        this.loadAssessments();
        this.loadAvailableAssessments();
        this.loadMyScorecardEmails();
        if (this.frameworkId !== null) this.loadGapReport();
      },
      error: () => {
        this.submittingAssessment = false;
        this.errorMessage = 'Could not submit the assessment. Check your answers and whether this attempt was already submitted.';
      }
    });
  }

  viewEmailPreview(result: LearnerAttemptResult): void {
    this.activeEmailModal = {
      id: 0,
      attemptId: result.attemptId,
      assessmentId: result.assessmentId,
      assessmentTitle: result.assessmentTitle,
      recipientEmail: result.emailRecipient || this.profile?.email || 'student@skillsphere.com',
      recipientName: this.profile?.name || 'Student Learner',
      scorePoints: result.scorePoints,
      maxPoints: result.maxPoints,
      percentage: result.percentage,
      passed: result.passed,
      subject: result.emailSubject || `[SkillSphere Nexus] Assessment Scorecard: ${result.assessmentTitle}`,
      bodyText: result.emailBodyPreview || `Official Scorecard Email for ${result.assessmentTitle}\nMarks: ${result.scorePoints}/${result.maxPoints} (${result.percentage}%)\nStatus: ${result.passed ? 'PASSED' : 'NOT PASSED'}`,
      status: 'DELIVERED',
      sentAt: result.submittedAt || new Date().toISOString()
    };
  }

  openEmailModal(email: ScorecardEmail): void {
    this.activeEmailModal = email;
  }

  private loadRecommendations(): void {
    if (this.frameworkId === null) {
      this.recommendations = [];
      return;
    }
    this.recommendationsLoading = true;
    this.learningService.getRecommendations(this.frameworkId).subscribe({
      next: recommendations => {
        this.recommendations = recommendations;
        this.recommendationsLoading = false;
      },
      error: () => {
        this.recommendationsLoading = false;
        this.errorMessage = 'Could not load learning recommendations.';
      }
    });
  }
}
