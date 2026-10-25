import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Assessment, CompetencyFramework, CompetencyGapReport, EmployeeSkillAssignment, IssuedCertificate, Student } from './feature-models';
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
          <p class="eyebrow">SKILLSPHERE NEXUS · STUDENT</p>
          <h1>Your learning workspace</h1>
          <p>Track verified skills, assessments, competency gaps, and certificates.</p>
        </div>
        <button class="secondary-btn" type="button" (click)="refresh()">Refresh</button>
      </header>

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
            {{ profileSaving ? 'Saving…' : 'Update profile' }}
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
          <label>Self-reported proficiency (1–10)
            <input type="number" name="proficiency" min="1" max="10" [(ngModel)]="proficiency" required />
          </label>
          <button class="primary-btn" type="submit" [disabled]="skillForm.invalid || skillSaving">
            {{ skillSaving ? 'Requesting…' : 'Request skill' }}
          </button>
        </form>
        <p *ngIf="skillsLoading">Loading your skills...</p>
        <div class="table-wrap" *ngIf="!skillsLoading">
          <table>
            <thead><tr><th>SKILL</th><th>SELF-REPORTED LEVEL</th><th>VERIFICATION</th></tr></thead>
            <tbody>
              <tr *ngFor="let item of assignments">
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
            <option *ngFor="let framework of frameworks" [ngValue]="framework.id">{{ framework.roleTitle }} — {{ framework.name }}</option>
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
                  <td>{{ gap.employeeProficiency ?? '—' }}</td>
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
        <h2>Assessment history</h2>
        <p>Assessment results are recorded and verified by authorized assessors; student-supplied scores are not accepted.</p>
        <p *ngIf="assessmentLoading">Loading assessment history...</p>
        <div class="table-wrap" *ngIf="!assessmentLoading">
          <table>
            <thead><tr><th>SKILL</th><th>RESULT</th><th>METHOD</th><th>DATE</th><th>STATUS</th></tr></thead>
            <tbody>
              <tr *ngFor="let item of assessments">
                <td>{{ item.skill?.name || 'Skill' }}</td>
                <td>{{ item.score }}/{{ item.maxScore }}</td>
                <td>{{ item.assessmentMethod }}</td>
                <td>{{ item.assessmentDate }}</td>
                <td>{{ item.verificationStatus }}</td>
              </tr>
              <tr *ngIf="assessments.length === 0"><td colspan="5">No assessments have been recorded for you.</td></tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <h2>Online assessment attempts</h2>
        <div class="table-wrap">
          <table><thead><tr><th>ASSESSMENT</th><th>ATTEMPT</th><th>RESULT</th><th>SUBMITTED</th><th>STATUS</th></tr></thead>
            <tbody><tr *ngFor="let item of attemptHistory">
              <td>{{ item.assessmentTitle }}</td><td>{{ item.attemptNumber }}</td>
              <td>{{ item.scorePoints }}/{{ item.maxPoints }} ({{ item.percentage }}%)</td>
              <td>{{ item.submittedAt }}</td><td>{{ item.passed ? 'PASSED' : 'NOT PASSED' }}</td>
            </tr><tr *ngIf="attemptHistory.length === 0"><td colspan="5">No online assessment attempts submitted yet.</td></tr></tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <h2>Available assessments</h2>
        <p>Questions are scored on the server. Correct answers are not sent to your browser.</p>
        <p *ngIf="availableAssessmentsLoading">Loading available assessments...</p>
        <div class="assessment-card" *ngFor="let assessment of availableAssessments">
          <div><h3>{{ assessment.title }} · {{ assessment.skillName }}</h3><p>{{ assessment.instructions }}</p>
            <small>{{ assessment.questionCount }} questions · passing score {{ assessment.passPercentage }}% · {{ assessment.attemptsRemaining }} attempt(s) remaining</small>
          </div>
          <button class="primary-btn" type="button" (click)="startAssessment(assessment)" [disabled]="startingAssessmentId === assessment.id">
            {{ startingAssessmentId === assessment.id ? 'Starting…' : 'Start assessment' }}
          </button>
        </div>
        <p *ngIf="!availableAssessmentsLoading && availableAssessments.length === 0">No active assessments are available for your profile right now.</p>
        <div class="attempt-panel" *ngIf="activeAttempt as attempt">
          <h3>{{ attempt.assessment.title }} · Attempt {{ attempt.attemptNumber }}</h3>
          <p>{{ attempt.assessment.instructions }}</p>
          <fieldset *ngFor="let question of attempt.questions; let questionNumber = index">
            <legend>{{ questionNumber + 1 }}. {{ question.prompt }} ({{ question.points }} point(s), {{ question.difficulty | lowercase }})</legend>
            <label class="answer-option" *ngFor="let option of question.options; let optionIndex = index">
              <input type="radio" [name]="'question-' + question.id" [value]="optionIndex" [(ngModel)]="question.selectedOptionIndex" />
              {{ option }}
            </label>
          </fieldset>
          <button class="primary-btn" type="button" (click)="submitAssessment()" [disabled]="submittingAssessment || !allQuestionsAnswered(attempt)">
            {{ submittingAssessment ? 'Submitting…' : 'Submit answers' }}
          </button>
        </div>
      </section>

      <section class="panel" *ngIf="latestAttemptResult as result">
        <h2>Latest assessment result</h2>
        <p class="success-message" *ngIf="result.passed">Passed with {{ result.percentage }}% (required {{ result.passPercentage }}%).</p>
        <p *ngIf="!result.passed">Not passed: {{ result.percentage }}% (required {{ result.passPercentage }}%). You may use any remaining attempts.</p>
        <p>Score {{ result.scorePoints }}/{{ result.maxPoints }} · submitted {{ result.submittedAt }}</p>
        <button class="primary-btn" type="button" *ngIf="result.certificate" (click)="download(result.certificate)">
          Download {{ result.certificate.certificateNumber }} PDF
        </button>
        <p *ngIf="result.passed && !result.certificate">Assessment passed. No certificate met the configured certificate issuance threshold.</p>
      </section>

      <section class="panel">
        <h2>Resources for your selected skill gaps</h2>
        <p *ngIf="recommendationsLoading">Finding configured resources for your gaps...</p>
        <div class="resource-card" *ngFor="let recommendation of recommendations">
          <div><strong>{{ recommendation.resource.title }} · {{ recommendation.resource.skillName }}</strong>
            <p>{{ recommendation.resource.description }}</p>
            <small>Gap {{ recommendation.priorityGap }} level(s) · current {{ recommendation.currentVerifiedProficiency }}/10 · required {{ recommendation.requiredProficiency }}/10 · {{ recommendation.resource.resourceType }} · {{ recommendation.resource.difficultyLevel }}</small>
          </div>
          <a *ngIf="recommendation.resource.verifiedUrl" [href]="recommendation.resource.verifiedUrl" target="_blank" rel="noopener noreferrer">Open resource</a>
        </div>
        <p *ngIf="!recommendationsLoading && frameworkId !== null && recommendations.length === 0">No learning resource is currently configured for your skill gaps.</p>
        <p *ngIf="frameworkId === null">Choose a competency framework above to find matching resources.</p>
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
        <div class="table-wrap" *ngIf="!certificatesLoading">
          <table>
            <thead><tr><th>SKILL</th><th>CERTIFICATE</th><th>ISSUED</th><th>STATUS</th><th>ACTION</th></tr></thead>
            <tbody>
              <tr *ngFor="let certificate of certificates">
                <td>{{ certificate.skillName }}<small>{{ certificate.proficiencyLevel }}/10 verified</small></td>
                <td>{{ certificate.certificateNumber }}<small>{{ certificate.issuerName }}</small></td>
                <td>{{ certificate.issueDate }}</td>
                <td>{{ certificate.status }}</td>
                <td class="actions">
                  <button class="primary-btn" type="button" (click)="download(certificate)" [disabled]="downloadingId === certificate.id || certificate.status !== 'ISSUED'">
                    {{ downloadingId === certificate.id ? 'Downloading…' : 'Download PDF' }}
                  </button>
                  <a [href]="verificationUrl(certificate)" target="_blank" rel="noopener">Verify</a>
                </td>
              </tr>
              <tr *ngIf="certificates.length === 0"><td colspan="5">No achievement certificates yet. Verified passing assessments will appear here.</td></tr>
            </tbody>
          </table>
        </div>
      </section>

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
  activeAttempt: StartedAssessment | null = null;
  latestAttemptResult: LearnerAttemptResult | null = null;
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
  successMessage = '';

  constructor(
    private readonly studentService: StudentService,
    private readonly skillService: SkillService,
    private readonly frameworkService: FrameworkService,
    private readonly certificateService: IssuedCertificateService,
    private readonly learningService: LearningService,
    private readonly authService: AuthService,
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
        this.profileLoading = false;
        this.errorMessage = 'Could not load your student profile.';
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

  private loadAvailableAssessments(): void {
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
          ? 'Assessment submitted and passed.'
          : 'Assessment submitted. Review your result and remaining attempts.';
        if (result.certificate) {
          this.certificates = [result.certificate,
            ...this.certificates.filter(item => item.id !== result.certificate?.id)];
          this.successMessage = 'Assessment passed and your achievement certificate is ready to download.';
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
        if (this.frameworkId !== null) this.loadGapReport();
      },
      error: () => {
        this.submittingAssessment = false;
        this.errorMessage = 'Could not submit the assessment. Check your answers and whether this attempt was already submitted.';
      }
    });
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
