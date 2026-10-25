
import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { SkillService } from './skill.service';
import { Skill } from './skill';
import { EmployeeSkillService } from './employee-skill.service';
import { AssessmentService } from './assessment.service';
import { FrameworkService } from './framework.service';
import { CertificationService } from './certification.service';
import { AuthService } from './auth.service';
import { IssuedCertificateService } from './issued-certificate.service';
import { CourseService } from './course.service';
import { CareerService } from './career.service';
import {
  Assessment,
  Certification,
  CompetencyFramework,
  CompetencyGap,
  EmployeeSkillAssignment,
  FrameworkSkillRequirement,
  IssuedCertificate,
  Course,
  CourseEnrollment,
  LearningPath,
  CareerPlan,
  InternalJob,
  JobApplication
} from './feature-models';

interface Employee {
  id?: number;
  name: string;
  email: string;
  jobTitle: string;
  department: string;
}

@Component({
  selector: 'app-workspace',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class WorkspaceComponent implements OnInit {
  currentPage:
    | 'dashboard'
    | 'employees'
    | 'skills'
    | 'mappings'
    | 'assessments'
    | 'frameworks'
    | 'certifications'
    | 'courses'
    | 'career' = 'dashboard';


  employees: Employee[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';
  showForm = false;
  editingId: number | null = null;
  employee: Employee = this.emptyEmployee();
  private readonly employeeApiUrl = 'http://localhost:8080/api/employees';

  skills: Skill[] = [];
  skillsLoading = false;
  skillErrorMessage = '';
  skillSuccessMessage = '';
  showSkillForm = false;
  editingSkillId: number | null = null;
  skill: Skill = this.emptySkill();

  employeeSkillAssignments: EmployeeSkillAssignment[] = [];
  employeeSkillLoading = false;
  employeeSkillError = '';
  employeeSkillSuccess = '';
  showAssignmentForm = false;
  editingAssignmentId: number | null = null;
  assignment: EmployeeSkillAssignment = this.emptyAssignment();

  assessments: Assessment[] = [];
  assessmentLoading = false;
  assessmentError = '';
  assessmentSuccess = '';
  showAssessmentForm = false;
  editingAssessmentId: number | null = null;
  assessment: Assessment = this.emptyAssessment();

  frameworks: CompetencyFramework[] = [];
  frameworkLoading = false;
  frameworkError = '';
  frameworkSuccess = '';
  selectedFrameworkId: number | null = null;
  frameworkFormVisible = false;
  editingFrameworkId: number | null = null;
  framework: CompetencyFramework = this.emptyFramework();
  frameworkRequirements: FrameworkSkillRequirement[] = [];
  requirementFormVisible = false;
  editingRequirementId: number | null = null;
  requirement: FrameworkSkillRequirement = this.emptyRequirement();
  selectedGapEmployeeId: number | null = null;
  gapReport: CompetencyGap[] = [];

  certifications: Certification[] = [];
  achievementCertificates: IssuedCertificate[] = [];
  certificationLoading = false;
  certificationError = '';
  certificationSuccess = '';
  achievementCertificateLoading = false;
  achievementCertificateError = '';
  downloadingAchievementId: number | null = null;
  showCertificationForm = false;
  editingCertificationId: number | null = null;
  certification: Certification = this.emptyCertification();

  // Milestone 2 LMS State
  courses: Course[] = [];
  learningPaths: LearningPath[] = [];
  myEnrollments: CourseEnrollment[] = [];
  courseLoading = false;
  courseError = '';
  courseSuccess = '';
  newCourse: Course = { title: '', description: '', durationHours: 40, instructor: '', courseType: 'ONLINE', rating: 4.8, totalEnrolled: 0, totalCompleted: 0, category: 'Technical' };
  showCourseForm = false;

  // Milestone 3 Compliance Summary State
  complianceSummary: any = null;
  expiringCertFilter = false;

  // Milestone 4 Career & Jobs State
  careerPlan: CareerPlan | null = null;
  internalJobs: InternalJob[] = [];
  myApplications: JobApplication[] = [];
  careerLoading = false;
  careerError = '';
  careerSuccess = '';
  showCareerForm = false;
  newJob: InternalJob = { title: '', department: 'Engineering', description: '', requiredSkills: '', openingsCount: 1, status: 'OPEN' };
  showJobForm = false;

  constructor(
    private http: HttpClient,
    private skillService: SkillService,
    private employeeSkillService: EmployeeSkillService,
    private assessmentService: AssessmentService,
    private frameworkService: FrameworkService,
    private certificationService: CertificationService,
    private issuedCertificateService: IssuedCertificateService,
    public courseService: CourseService,
    public careerService: CareerService,
    @Inject(AuthService) public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCourses();
    this.loadLearningPaths();
    this.loadComplianceSummary();
    this.loadInternalJobs();

    if (this.authService.isHrAdmin) {
      this.loadEmployees();
      this.loadSkills();
      this.loadEmployeeSkillAssignments();
      this.loadAssessments();
      this.loadFrameworks();
      this.loadCertifications();
      return;
    }
    this.loadSkills();
    this.loadCurrentEmployeeData();
    this.loadMyEnrollments();
    this.loadMyCareerPlan();
    this.loadMyApplications();
  }

  get isHrAdmin(): boolean {
    return this.authService.isHrAdmin;
  }

  get isEmployee(): boolean {
    return this.authService.isEmployee;
  }

  get isStudent(): boolean {
    return this.authService.isStudent;
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login');
  }

  goToStudentAccounts(): void {
    void this.router.navigateByUrl('/hr/students');
  }

  goToAdminLearning(): void {
    void this.router.navigateByUrl('/hr/learning');
  }

  goToStudentPortal(): void {
    void this.router.navigateByUrl('/student');
  }

  private loadCurrentEmployeeData(): void {
    this.http.get<Employee>(`${this.employeeApiUrl}/me`).subscribe({
      next: (employee) => {
        this.employees = [employee];
        this.selectedGapEmployeeId = employee.id ?? null;
        this.employeeSkillService.getAssignmentsByEmployee(employee.id!).subscribe({
          next: (data) => this.employeeSkillAssignments = Array.isArray(data) ? data : [],
          error: () => this.employeeSkillError = 'Could not load your skill assignments.'
        });
        this.assessmentService.getAssessmentsByEmployee(employee.id!).subscribe({
          next: (data) => this.assessments = Array.isArray(data) ? data : [],
          error: () => this.assessmentError = 'Could not load your assessments.'
        });
        this.certificationService.getCertificationsByEmployee(employee.id!).subscribe({
          next: (data) => this.certifications = Array.isArray(data) ? data : [],
          error: () => this.certificationError = 'Could not load your certifications.'
        });
        this.loadFrameworks();
      },
      error: () => this.errorMessage = 'Could not load your employee profile.'
    });
  }

  emptyEmployee(): Employee {
    return { name: '', email: '', jobTitle: '', department: '' };
  }

  emptySkill(): Skill {
    return { name: '', category: 'Technical', description: '' };
  }

  emptyAssignment(): EmployeeSkillAssignment {
    return {
      employeeId: undefined,
      skillId: undefined,
      proficiency: 5,
      verificationStatus: 'PENDING_VERIFICATION',
      assessmentDate: new Date().toISOString().slice(0, 10)
    };
  }

  emptyAssessment(): Assessment {
    return {
      employeeId: undefined,
      skillId: undefined,
      score: 0,
      maxScore: 100,
      assessmentDate: new Date().toISOString().slice(0, 10),
      assessmentMethod: 'Practical',
      assessorName: ''
    };
  }

  emptyFramework(): CompetencyFramework {
    return { name: '', roleTitle: '', description: '' };
  }

  emptyRequirement(): FrameworkSkillRequirement {
    return { skillId: undefined, requiredProficiency: 5, notes: '' };
  }

  emptyCertification(): Certification {
    return {
      employeeId: undefined,
      name: '',
      issuingOrganization: '',
      credentialId: '',
      issueDate: new Date().toISOString().slice(0, 10),
      expiryDate: '',
      verificationStatus: 'PENDING_VERIFICATION'
    };
  }

  loadEmployees(): void {
    this.loading = true;
    this.errorMessage = '';

    this.http.get<Employee[]>(this.employeeApiUrl).subscribe({
      next: (data) => {
        this.employees = Array.isArray(data) ? data : [];
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = 'Could not load employees. Check the backend and CORS settings.';
        console.error('Loading employees failed:', error);
      }
    });
  }

  openAddForm(): void {
    this.editingId = null;
    this.employee = this.emptyEmployee();
    this.showForm = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  openEditForm(item: Employee): void {
    this.editingId = item.id ?? null;
    this.employee = { ...item };
    this.showForm = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  saveEmployee(): void {
    this.errorMessage = '';
    this.successMessage = '';

    const editing = this.editingId !== null;
    const request = editing
      ? this.http.put<Employee>(`${this.employeeApiUrl}/${this.editingId}`, this.employee)
      : this.http.post<Employee>(this.employeeApiUrl, this.employee);

    request.subscribe({
      next: () => {
        this.showForm = false;
        this.successMessage = editing ? 'Employee updated successfully.' : 'Employee added successfully.';
        this.editingId = null;
        this.employee = this.emptyEmployee();
        this.loadEmployees();
      },
      error: (error) => {
        this.errorMessage =
          error.status === 0
            ? 'Cannot reach the backend. Check CORS and port 8080.'
            : 'Could not save employee. Check the details and email address.';
        console.error('Saving employee failed:', error);
      }
    });
  }

  deleteEmployee(item: Employee): void {
    if (item.id == null || !confirm(`Delete ${item.name}?`)) {
      return;
    }

    this.http.delete<void>(`${this.employeeApiUrl}/${item.id}`).subscribe({
      next: () => {
        this.successMessage = 'Employee deleted successfully.';
        this.loadEmployees();
      },
      error: (error) => {
        this.errorMessage = 'Could not delete employee.';
        console.error('Deleting employee failed:', error);
      }
    });
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.employee = this.emptyEmployee();
  }

  loadSkills(): void {
    this.skillsLoading = true;
    this.skillErrorMessage = '';

    this.skillService.getSkills().subscribe({
      next: (data) => {
        this.skills = Array.isArray(data) ? data : [];
        this.skillsLoading = false;
      },
      error: (error) => {
        this.skillsLoading = false;
        this.skillErrorMessage = 'Could not load skills. Check the backend and CORS settings.';
        console.error('Loading skills failed:', error);
      }
    });
  }

  openAddSkillForm(): void {
    this.editingSkillId = null;
    this.skill = this.emptySkill();
    this.showSkillForm = true;
    this.skillErrorMessage = '';
    this.skillSuccessMessage = '';
  }

  openEditSkillForm(item: Skill): void {
    this.editingSkillId = item.id ?? null;
    this.skill = { ...item };
    this.showSkillForm = true;
    this.skillErrorMessage = '';
    this.skillSuccessMessage = '';
  }

  saveSkill(): void {
    this.skillErrorMessage = '';
    this.skillSuccessMessage = '';

    const editing = this.editingSkillId !== null;
    const request = editing
      ? this.skillService.updateSkill(this.editingSkillId!, this.skill)
      : this.skillService.createSkill(this.skill);

    request.subscribe({
      next: () => {
        this.showSkillForm = false;
        this.skillSuccessMessage = editing ? 'Skill updated successfully.' : 'Skill added successfully.';
        this.editingSkillId = null;
        this.skill = this.emptySkill();
        this.loadSkills();
      },
      error: (error) => {
        this.skillErrorMessage =
          error.status === 0
            ? 'Cannot reach the backend. Check port 8080 and CORS.'
            : 'Could not save skill. Check the details.';
        console.error('Saving skill failed:', error);
      }
    });
  }

  deleteSkill(item: Skill): void {
    if (item.id == null || !confirm(`Delete ${item.name}?`)) {
      return;
    }

    this.skillService.deleteSkill(item.id).subscribe({
      next: () => {
        this.skillSuccessMessage = 'Skill deleted successfully.';
        this.loadSkills();
      },
      error: (error) => {
        this.skillErrorMessage = 'Could not delete skill.';
        console.error('Deleting skill failed:', error);
      }
    });
  }

  cancelSkillForm(): void {
    this.showSkillForm = false;
    this.editingSkillId = null;
    this.skill = this.emptySkill();
  }

  loadEmployeeSkillAssignments(): void {
    this.employeeSkillLoading = true;
    this.employeeSkillError = '';

    const request = this.isHrAdmin
      ? this.employeeSkillService.getAssignments()
      : this.employeeSkillService.getAssignmentsByEmployee(this.employees[0]?.id ?? 0);
    request.subscribe({
      next: (data) => {
        this.employeeSkillAssignments = Array.isArray(data) ? data : [];
        this.employeeSkillLoading = false;
      },
      error: (error) => {
        this.employeeSkillLoading = false;
        this.employeeSkillError = 'Could not load employee-skill assignments.';
        console.error('Loading assignments failed:', error);
      }
    });
  }

  openAddAssignmentForm(): void {
    this.editingAssignmentId = null;
    this.assignment = this.emptyAssignment();
    this.showAssignmentForm = true;
    this.employeeSkillError = '';
    this.employeeSkillSuccess = '';
  }

  openEditAssignmentForm(item: EmployeeSkillAssignment): void {
    this.editingAssignmentId = item.id ?? null;
    this.assignment = {
      employeeId: item.employeeId ?? item.employee?.id,
      skillId: item.skillId ?? item.skill?.id,
      proficiency: item.proficiency,
      verificationStatus: item.verificationStatus ?? 'PENDING_VERIFICATION',
      assessmentDate: item.assessmentDate ?? new Date().toISOString().slice(0, 10)
    };
    this.showAssignmentForm = true;
    this.employeeSkillError = '';
    this.employeeSkillSuccess = '';
  }

  saveAssignment(): void {
    this.employeeSkillError = '';
    this.employeeSkillSuccess = '';

    if (!this.assignment.employeeId || !this.assignment.skillId) {
      this.employeeSkillError = 'Select both an employee and a skill.';
      return;
    }

    const payload: EmployeeSkillAssignment = {
      ...this.assignment,
      employeeId: this.assignment.employeeId,
      skillId: this.assignment.skillId,
      verificationStatus: this.assignment.verificationStatus ?? 'PENDING_VERIFICATION'
    };

    const request = this.editingAssignmentId !== null
      ? this.employeeSkillService.updateAssignment(this.editingAssignmentId, payload)
      : this.employeeSkillService.createAssignment(payload);

    request.subscribe({
      next: () => {
        this.showAssignmentForm = false;
        this.employeeSkillSuccess = this.editingAssignmentId !== null
          ? 'Assignment updated successfully.'
          : 'Assignment created successfully.';
        this.editingAssignmentId = null;
        this.assignment = this.emptyAssignment();
        this.loadEmployeeSkillAssignments();
      },
      error: (error) => {
        this.employeeSkillError =
          error.status === 409
            ? 'This employee already has this skill assigned.'
            : (error.status === 0 ? 'Cannot reach the backend.' : 'Could not save assignment.');
        console.error('Saving assignment failed:', error);
      }
    });
  }

  deleteAssignment(item: EmployeeSkillAssignment): void {
    if (item.id == null || !confirm('Remove this employee-skill assignment?')) {
      return;
    }

    this.employeeSkillService.deleteAssignment(item.id).subscribe({
      next: () => {
        this.employeeSkillSuccess = 'Assignment removed successfully.';
        this.loadEmployeeSkillAssignments();
      },
      error: (error) => {
        this.employeeSkillError = 'Could not remove assignment.';
        console.error('Deleting assignment failed:', error);
      }
    });
  }

  loadAssessments(): void {
    this.assessmentLoading = true;
    this.assessmentError = '';

    const request = this.isHrAdmin
      ? this.assessmentService.getAssessments()
      : this.assessmentService.getAssessmentsByEmployee(this.employees[0]?.id ?? 0);
    request.subscribe({
      next: (data) => {
        this.assessments = Array.isArray(data) ? data : [];
        this.assessmentLoading = false;
      },
      error: (error) => {
        this.assessmentLoading = false;
        this.assessmentError = 'Could not load assessments.';
        console.error('Loading assessments failed:', error);
      }
    });
  }

  openAddAssessmentForm(): void {
    this.editingAssessmentId = null;
    this.assessment = this.emptyAssessment();
    this.showAssessmentForm = true;
    this.assessmentError = '';
    this.assessmentSuccess = '';
  }

  openEditAssessmentForm(item: Assessment): void {
    this.editingAssessmentId = item.id ?? null;
    this.assessment = {
      employeeId: item.employeeId ?? item.employee?.id,
      skillId: item.skillId ?? item.skill?.id,
      score: item.score,
      maxScore: item.maxScore,
      assessmentDate: item.assessmentDate,
      assessmentMethod: item.assessmentMethod,
      assessorName: item.assessorName ?? ''
    };
    this.showAssessmentForm = true;
    this.assessmentError = '';
    this.assessmentSuccess = '';
  }

  saveAssessment(): void {
    this.assessmentError = '';
    this.assessmentSuccess = '';

    if (!this.assessment.employeeId || !this.assessment.skillId) {
      this.assessmentError = 'Select both an employee and a skill.';
      return;
    }

    const payload: Assessment = {
      employeeId: this.assessment.employeeId,
      skillId: this.assessment.skillId,
      score: this.assessment.score,
      maxScore: this.assessment.maxScore,
      assessmentDate: this.assessment.assessmentDate,
      assessmentMethod: this.assessment.assessmentMethod,
      assessorName: this.assessment.assessorName ?? ''
    };

    const request = this.editingAssessmentId !== null
      ? this.assessmentService.updateAssessment(this.editingAssessmentId, payload)
      : this.assessmentService.createAssessment(payload);

    request.subscribe({
      next: () => {
        this.showAssessmentForm = false;
        this.assessmentSuccess = this.editingAssessmentId !== null ? 'Assessment updated successfully.' : 'Assessment created successfully.';
        this.editingAssessmentId = null;
        this.assessment = this.emptyAssessment();
        this.loadAssessments();
      },
      error: (error) => {
        this.assessmentError = error.status === 0 ? 'Cannot reach the backend.' : 'Assessment validation failed. Check the score values and date.';
        console.error('Saving assessment failed:', error);
      }
    });
  }

  deleteAssessment(item: Assessment): void {
    if (item.id == null || !confirm('Delete this assessment?')) {
      return;
    }

    this.assessmentService.deleteAssessment(item.id).subscribe({
      next: () => {
        this.assessmentSuccess = 'Assessment deleted successfully.';
        this.loadAssessments();
      },
      error: (error) => {
        this.assessmentError = 'Could not delete assessment.';
        console.error('Deleting assessment failed:', error);
      }
    });
  }

  verifyAssessment(item: Assessment, status: 'VERIFIED' | 'REJECTED'): void {
    if (item.id == null) {
      return;
    }

    this.assessmentService.verifyAssessment(item.id, status).subscribe({
      next: () => {
        this.assessmentSuccess = status === 'VERIFIED' ? 'Assessment verified.' : 'Assessment rejected.';
        this.loadAssessments();
        this.loadAchievementCertificates();
      },
      error: (error) => {
        this.assessmentError = 'Could not update assessment verification.';
        console.error('Verifying assessment failed:', error);
      }
    });
  }

  loadFrameworks(): void {
    this.frameworkLoading = true;
    this.frameworkError = '';

    this.frameworkService.getFrameworks().subscribe({
      next: (data) => {
        this.frameworks = Array.isArray(data) ? data : [];
        this.frameworkLoading = false;
        if (this.frameworks.length > 0 && this.selectedFrameworkId == null) {
          this.selectedFrameworkId = this.frameworks[0].id ?? null;
          this.loadFrameworkRequirements();
          if (this.selectedGapEmployeeId != null) {
            this.loadGapReport();
          }
        }
      },
      error: (error) => {
        this.frameworkLoading = false;
        this.frameworkError = 'Could not load competency frameworks.';
        console.error('Loading frameworks failed:', error);
      }
    });
  }

  openAddFrameworkForm(): void {
    this.editingFrameworkId = null;
    this.framework = this.emptyFramework();
    this.frameworkFormVisible = true;
    this.frameworkError = '';
    this.frameworkSuccess = '';
  }

  openEditFrameworkForm(item: CompetencyFramework): void {
    this.editingFrameworkId = item.id ?? null;
    this.framework = { ...item };
    this.frameworkFormVisible = true;
    this.frameworkError = '';
    this.frameworkSuccess = '';
  }

  saveFramework(): void {
    this.frameworkError = '';
    this.frameworkSuccess = '';

    const payload: CompetencyFramework = {
      ...this.framework,
      name: this.framework.name,
      roleTitle: this.framework.roleTitle,
      description: this.framework.description ?? ''
    };

    const request = this.editingFrameworkId !== null
      ? this.frameworkService.updateFramework(this.editingFrameworkId, payload)
      : this.frameworkService.createFramework(payload);

    request.subscribe({
      next: (result) => {
        this.frameworkFormVisible = false;
        this.frameworkSuccess = this.editingFrameworkId !== null ? 'Framework updated.' : 'Framework created.';
        this.editingFrameworkId = null;
        this.framework = this.emptyFramework();
        this.loadFrameworks();
        this.selectedFrameworkId = result.id ?? null;
      },
      error: (error) => {
        this.frameworkError = error.status === 0 ? 'Cannot reach the backend.' : 'Could not save framework.';
        console.error('Saving framework failed:', error);
      }
    });
  }

  deleteFramework(item: CompetencyFramework): void {
    if (item.id == null || !confirm(`Delete ${item.name}?`)) {
      return;
    }

    this.frameworkService.deleteFramework(item.id).subscribe({
      next: () => {
        this.frameworkSuccess = 'Framework deleted.';
        this.loadFrameworks();
      },
      error: (error) => {
        this.frameworkError = 'Could not delete framework.';
        console.error('Deleting framework failed:', error);
      }
    });
  }

  selectFramework(frameworkId: number | null): void {
    this.selectedFrameworkId = frameworkId;
    this.loadFrameworkRequirements();
    this.loadGapReport();
  }

  loadFrameworkRequirements(): void {
    if (this.selectedFrameworkId == null) {
      this.frameworkRequirements = [];
      return;
    }

    this.frameworkService.getRequirements(this.selectedFrameworkId).subscribe({
      next: (data) => {
        this.frameworkRequirements = Array.isArray(data) ? data : [];
      },
      error: (error) => {
        this.frameworkError = 'Could not load framework requirements.';
        console.error('Loading framework requirements failed:', error);
      }
    });
  }

  openAddRequirementForm(): void {
    this.editingRequirementId = null;
    this.requirement = this.emptyRequirement();
    this.requirementFormVisible = true;
  }

  openEditRequirementForm(item: FrameworkSkillRequirement): void {
    this.editingRequirementId = item.id ?? null;
    this.requirement = {
      skillId: item.skillId ?? item.skill?.id,
      requiredProficiency: item.requiredProficiency,
      notes: item.notes ?? ''
    };
    this.requirementFormVisible = true;
  }

  saveRequirement(): void {
    if (this.selectedFrameworkId == null || !this.requirement.skillId) {
      this.frameworkError = 'Select a framework and a skill before saving the requirement.';
      return;
    }

    const payload = {
      ...this.requirement,
      skillId: this.requirement.skillId,
      requiredProficiency: this.requirement.requiredProficiency
    };

    const request = this.editingRequirementId !== null
      ? this.frameworkService.updateRequirement(this.selectedFrameworkId, this.editingRequirementId, payload)
      : this.frameworkService.addRequirement(this.selectedFrameworkId, payload);

    request.subscribe({
      next: () => {
        this.requirementFormVisible = false;
        this.frameworkSuccess = this.editingRequirementId !== null ? 'Requirement updated.' : 'Requirement added.';
        this.editingRequirementId = null;
        this.requirement = this.emptyRequirement();
        this.loadFrameworkRequirements();
      },
      error: (error) => {
        this.frameworkError = error.status === 0 ? 'Cannot reach the backend.' : 'Could not save requirement.';
        console.error('Saving requirement failed:', error);
      }
    });
  }

  deleteRequirement(item: FrameworkSkillRequirement): void {
    if (this.selectedFrameworkId == null || item.id == null || !confirm('Delete this framework requirement?')) {
      return;
    }

    this.frameworkService.deleteRequirement(this.selectedFrameworkId, item.id).subscribe({
      next: () => {
        this.frameworkSuccess = 'Requirement deleted.';
        this.loadFrameworkRequirements();
      },
      error: (error) => {
        this.frameworkError = 'Could not delete requirement.';
        console.error('Deleting requirement failed:', error);
      }
    });
  }

  loadGapReport(): void {
    if (this.selectedFrameworkId == null || this.selectedGapEmployeeId == null) {
      this.gapReport = [];
      return;
    }

    this.frameworkService.getGapReport(this.selectedFrameworkId, this.selectedGapEmployeeId).subscribe({
      next: (data) => {
        this.gapReport = Array.isArray(data) ? data : [];
      },
      error: (error) => {
        this.frameworkError = 'Could not load skill gap report.';
        console.error('Loading gap report failed:', error);
      }
    });
  }

  loadCertifications(): void {
    this.certificationLoading = true;
    this.certificationError = '';

    const request = this.isHrAdmin
      ? this.certificationService.getCertifications()
      : this.certificationService.getCertificationsByEmployee(this.employees[0]?.id ?? 0);
    request.subscribe({
      next: (data) => {
        this.certifications = Array.isArray(data) ? data : [];
        this.certificationLoading = false;
      },
      error: (error) => {
        this.certificationLoading = false;
        this.certificationError = 'Could not load certifications.';
        console.error('Loading certifications failed:', error);
      }
    });
    this.loadAchievementCertificates();
  }

  loadAchievementCertificates(): void {
    this.achievementCertificateLoading = true;
    this.achievementCertificateError = '';
    this.issuedCertificateService.getCertificates().subscribe({
      next: (certificates) => {
        this.achievementCertificates = certificates;
        this.achievementCertificateLoading = false;
      },
      error: (error) => {
        this.achievementCertificateLoading = false;
        this.achievementCertificateError = 'Could not load achievement certificates.';
        console.error('Loading achievement certificates failed:', error);
      }
    });
  }

  downloadAchievementCertificate(certificate: IssuedCertificate): void {
    this.downloadingAchievementId = certificate.id;
    this.issuedCertificateService.saveDownload(certificate).subscribe({
      next: () => {
        this.downloadingAchievementId = null;
      },
      error: (error) => {
        this.downloadingAchievementId = null;
        this.achievementCertificateError = error.status === 410
          ? 'This certificate has been revoked and cannot be downloaded.'
          : 'Could not download the achievement certificate.';
        console.error('Downloading achievement certificate failed:', error);
      }
    });
  }

  revokeAchievementCertificate(certificate: IssuedCertificate): void {
    const reason = prompt('Enter the reason for revoking this certificate:');
    if (!reason?.trim()) {
      return;
    }
    this.issuedCertificateService.revokeCertificate(certificate.id, reason.trim()).subscribe({
      next: () => {
        this.certificationSuccess = `Certificate ${certificate.certificateNumber} was revoked.`;
        this.loadAchievementCertificates();
      },
      error: (error) => {
        this.achievementCertificateError = 'Could not revoke this certificate.';
        console.error('Revoking achievement certificate failed:', error);
      }
    });
  }

  openAddCertificationForm(): void {
    this.editingCertificationId = null;
    this.certification = this.emptyCertification();
    if (!this.isHrAdmin) {
      this.certification.employeeId = this.authService.currentUser?.employeeId ?? this.employees[0]?.id;
    }
    this.showCertificationForm = true;
    this.certificationError = '';
    this.certificationSuccess = '';
  }

  openEditCertificationForm(item: Certification): void {
    this.editingCertificationId = item.id ?? null;
    this.certification = {
      employeeId: item.employeeId ?? item.employee?.id ?? (this.isHrAdmin ? undefined : (this.authService.currentUser?.employeeId ?? this.employees[0]?.id)),
      name: item.name,
      issuingOrganization: item.issuingOrganization,
      credentialId: item.credentialId ?? '',
      issueDate: item.issueDate,
      expiryDate: item.expiryDate ?? '',
      verificationStatus: item.verificationStatus ?? 'PENDING_VERIFICATION'
    };
    this.showCertificationForm = true;
    this.certificationError = '';
    this.certificationSuccess = '';
  }

  saveCertification(): void {
    this.certificationError = '';
    this.certificationSuccess = '';

    if (!this.certification.employeeId && !this.isHrAdmin) {
      this.certification.employeeId = this.authService.currentUser?.employeeId ?? this.employees[0]?.id;
    }

    if (!this.certification.employeeId) {
      this.certificationError = 'Select an employee before saving the certification.';
      return;
    }

    const payload: Certification = {
      ...this.certification,
      employeeId: this.certification.employeeId,
      expiryDate: this.certification.expiryDate && this.certification.expiryDate.length > 0 ? this.certification.expiryDate : null,
      verificationStatus: this.certification.verificationStatus ?? 'PENDING_VERIFICATION'
    };

    const request = this.editingCertificationId !== null
      ? this.certificationService.updateCertification(this.editingCertificationId, payload)
      : this.certificationService.createCertification(payload);

    request.subscribe({
      next: () => {
        this.showCertificationForm = false;
        this.certificationSuccess = this.editingCertificationId !== null ? 'Certification updated.' : 'Certification created.';
        this.editingCertificationId = null;
        this.certification = this.emptyCertification();
        this.loadCertifications();
      },
      error: (error) => {
        this.certificationError = error.status === 0 ? 'Cannot reach the backend.' : 'Could not save certification.';
        console.error('Saving certification failed:', error);
      }
    });
  }

  deleteCertification(item: Certification): void {
    if (item.id == null || !confirm(`Delete ${item.name}?`)) {
      return;
    }

    this.certificationService.deleteCertification(item.id).subscribe({
      next: () => {
        this.certificationSuccess = 'Certification deleted.';
        this.loadCertifications();
      },
      error: (error) => {
        this.certificationError = 'Could not delete certification.';
        console.error('Deleting certification failed:', error);
      }
    });
  }

  getBadgeClass(status: string | undefined): string {
    switch (status) {
      case 'VERIFIED':
        return 'status-pill success';
      case 'REJECTED':
        return 'status-pill danger';
      case 'PENDING_VERIFICATION':
        return 'status-pill warning';
      case 'UNDER_REQUIRED':
        return 'status-pill warning';
      case 'NOT_ASSIGNED':
        return 'status-pill neutral';
      case 'MATCHED':
        return 'status-pill success';
      case 'ACTIVE':
        return 'status-pill success';
      case 'EXPIRING_SOON':
        return 'status-pill warning';
      case 'EXPIRED':
      case 'REVOKED':
        return 'status-pill danger';
      case 'ISSUED':
        return 'status-pill success';
      default:
        return 'status-pill neutral';
    }
  }

  // --- Milestone 2: Course & Learning Management ---
  loadCourses(): void {
    this.courseLoading = true;
    this.courseService.getAllCourses().subscribe({
      next: (data) => {
        this.courses = Array.isArray(data) ? data : [];
        this.courseLoading = false;
      },
      error: () => {
        this.courseLoading = false;
      }
    });
  }

  saveCourse(): void {
    if (!this.newCourse.title) return;
    this.courseService.createCourse(this.newCourse).subscribe({
      next: (created) => {
        this.courses.unshift(created);
        this.showCourseForm = false;
        this.courseSuccess = 'Course added successfully!';
        setTimeout(() => this.courseSuccess = '', 3500);
      },
      error: () => this.courseError = 'Failed to create course.'
    });
  }

  loadLearningPaths(): void {
    this.courseService.getAllLearningPaths().subscribe({
      next: (data) => this.learningPaths = Array.isArray(data) ? data : []
    });
  }

  loadMyEnrollments(): void {
    this.courseService.getMyEnrollments().subscribe({
      next: (data) => this.myEnrollments = Array.isArray(data) ? data : []
    });
  }

  isEnrolled(courseId: number | undefined): boolean {
    if (!courseId) return false;
    return this.myEnrollments.some(e => e.course?.id === courseId);
  }

  getEnrollment(courseId: number | undefined): CourseEnrollment | undefined {
    if (!courseId) return undefined;
    return this.myEnrollments.find(e => e.course?.id === courseId);
  }

  enrollInCourse(courseId: number | undefined): void {
    if (!courseId) return;
    this.courseService.enroll(courseId).subscribe({
      next: (enrollment) => {
        this.myEnrollments.push(enrollment);
        this.courseSuccess = 'Enrolled in course successfully!';
        setTimeout(() => this.courseSuccess = '', 3500);
      },
      error: () => this.courseError = 'Enrollment failed.'
    });
  }

  updateEnrollmentProgress(enrollmentId: number | undefined, progress: number): void {
    if (!enrollmentId) return;
    this.courseService.updateProgress(enrollmentId, progress).subscribe({
      next: (updated) => {
        const idx = this.myEnrollments.findIndex(e => e.id === updated.id);
        if (idx !== -1) this.myEnrollments[idx] = updated;
      }
    });
  }

  // --- Milestone 3: Expiry Tracking & Renewal ---
  loadComplianceSummary(): void {
    this.certificationService.getComplianceMetrics().subscribe({
      next: (data) => this.complianceSummary = data,
      error: () => {}
    });
  }

  renewCertificationRecord(id: number | undefined): void {
    if (!id) return;
    this.certificationService.renewCertification(id).subscribe({
      next: (updated) => {
        const idx = this.certifications.findIndex(c => c.id === updated.id);
        if (idx !== -1) this.certifications[idx] = updated;
        this.certificationSuccess = 'Certification renewed successfully! Valid for next 2 years.';
        this.loadComplianceSummary();
        setTimeout(() => this.certificationSuccess = '', 4000);
      },
      error: () => this.certificationError = 'Failed to renew certification.'
    });
  }

  // --- Milestone 4: Career Planning & Job Portal ---
  loadMyCareerPlan(): void {
    this.careerService.getMyCareerPlan().subscribe({
      next: (plan) => {
        this.careerPlan = plan || {
          currentRole: this.employees[0]?.jobTitle || 'Developer',
          targetRole: 'Tech Lead',
          progressPercentage: 67,
          skillGaps: 'Angular +3, Leadership +2',
          mentorName: 'Jane Doe',
          promotionReadinessScore: 80,
          promotionEligibility: 'Eligible in 3 months',
          status: 'ACTIVE'
        };
      }
    });
  }

  saveCareerPlan(): void {
    if (!this.careerPlan) return;
    this.careerService.saveMyCareerPlan(this.careerPlan).subscribe({
      next: (saved) => {
        this.careerPlan = saved;
        this.showCareerForm = false;
        this.careerSuccess = 'Career plan updated successfully!';
        setTimeout(() => this.careerSuccess = '', 3500);
      },
      error: () => this.careerError = 'Could not save career plan.'
    });
  }

  loadInternalJobs(): void {
    this.careerService.getAllJobs().subscribe({
      next: (data) => this.internalJobs = Array.isArray(data) ? data : []
    });
  }

  saveJob(): void {
    if (!this.newJob.title || !this.newJob.requiredSkills) return;
    this.careerService.createJob(this.newJob).subscribe({
      next: (created) => {
        this.internalJobs.unshift(created);
        this.showJobForm = false;
        this.careerSuccess = 'Job opening posted successfully!';
        setTimeout(() => this.careerSuccess = '', 3500);
      },
      error: () => this.careerError = 'Failed to post job opening.'
    });
  }

  loadMyApplications(): void {
    this.careerService.getMyApplications().subscribe({
      next: (data) => this.myApplications = Array.isArray(data) ? data : []
    });
  }

  hasApplied(jobId: number | undefined): boolean {
    if (!jobId) return false;
    return this.myApplications.some(a => a.job?.id === jobId);
  }

  applyForJob(jobId: number | undefined): void {
    if (!jobId) return;
    this.careerService.applyForJob(jobId).subscribe({
      next: (app) => {
        this.myApplications.push(app);
        this.careerSuccess = 'Application submitted successfully!';
        setTimeout(() => this.careerSuccess = '', 3500);
      },
      error: () => this.careerError = 'Application failed.'
    });
  }

  // --- Visual Chart Getters for Dashboard ---
  get skillCategoryDonut(): { label: string; count: number; percent: number; color: string; dasharray: string; dashoffset: number }[] {
    const counts: Record<string, number> = { Technical: 0, 'Soft Skill': 0, Domain: 0 };
    for (const s of this.skills) {
      const cat = (s.category || '').toLowerCase();
      if (cat.includes('soft')) counts['Soft Skill']++;
      else if (cat.includes('domain')) counts['Domain']++;
      else counts['Technical']++;
    }
    const total = Object.values(counts).reduce((a, b) => a + b, 0) || 1;
    const colors: Record<string, string> = { Technical: '#3b82f6', 'Soft Skill': '#10b981', Domain: '#f59e0b' };
    const circumference = 376.99; // 2 * PI * 60
    let cumulative = 0;

    return Object.entries(counts).map(([label, count]) => {
      const fraction = count / total;
      const strokeLen = fraction * circumference;
      const emptyLen = circumference - strokeLen;
      const res = {
        label,
        count,
        percent: Math.round(fraction * 100),
        color: colors[label],
        dasharray: `${strokeLen.toFixed(1)} ${emptyLen.toFixed(1)}`,
        dashoffset: -cumulative
      };
      cumulative += strokeLen;
      return res;
    });
  }

  get certStatusDonut(): { label: string; count: number; percent: number; color: string; dasharray: string; dashoffset: number }[] {
    let valid = 0, expiring = 0, expired = 0;
    const now = new Date();
    const in30Days = new Date();
    in30Days.setDate(in30Days.getDate() + 30);

    for (const c of this.certifications) {
      if (c.expiryDate) {
        const exp = new Date(c.expiryDate);
        if (exp < now) expired++;
        else if (exp <= in30Days) expiring++;
        else valid++;
      } else {
        valid++;
      }
    }
    if (this.complianceSummary) {
      valid = this.complianceSummary.activeCount || valid;
      expiring = this.complianceSummary.expiringWithin30Days || expiring;
      expired = this.complianceSummary.expiredCount || expired;
    }
    const total = (valid + expiring + expired) || 1;
    const items = [
      { label: 'Active Valid', count: valid, color: '#10b981' },
      { label: 'Expiring in 30d', count: expiring, color: '#f59e0b' },
      { label: 'Expired', count: expired, color: '#ef4444' }
    ];
    const circumference = 376.99;
    let cumulative = 0;

    return items.map(item => {
      const fraction = item.count / total;
      const strokeLen = fraction * circumference;
      const emptyLen = circumference - strokeLen;
      const res = {
        label: item.label,
        count: item.count,
        percent: Math.round(fraction * 100),
        color: item.color,
        dasharray: `${strokeLen.toFixed(1)} ${emptyLen.toFixed(1)}`,
        dashoffset: -cumulative
      };
      cumulative += strokeLen;
      return res;
    });
  }

  get frameworkBarList(): { name: string; readiness: number; count: number; color: string }[] {
    if (this.frameworks.length === 0) {
      return [
        { name: 'Full Stack Java Cloud', readiness: 87, count: 8, color: 'linear-gradient(90deg, #3b82f6, #60a5fa)' },
        { name: 'DevOps & Kubernetes Engineer', readiness: 74, count: 6, color: 'linear-gradient(90deg, #10b981, #34d399)' },
        { name: 'Core Banking Architect', readiness: 92, count: 10, color: 'linear-gradient(90deg, #8b5cf6, #a78bfa)' }
      ];
    }
    const palette = [
      'linear-gradient(90deg, #3b82f6, #60a5fa)',
      'linear-gradient(90deg, #10b981, #34d399)',
      'linear-gradient(90deg, #8b5cf6, #a78bfa)',
      'linear-gradient(90deg, #f59e0b, #fbbf24)'
    ];
    return this.frameworks.slice(0, 5).map((f, i) => {
      const reqCount = f.skillRequirements?.length || 5;
      const simulatedReadiness = Math.min(100, Math.max(45, 60 + ((f.id || 1) * 7) % 38));
      return {
        name: f.name,
        readiness: simulatedReadiness,
        count: reqCount,
        color: palette[i % palette.length]
      };
    });
  }

  get proficiencyBarList(): { label: string; range: string; count: number; percent: number; color: string }[] {
    let beginner = 0, intermediate = 0, advanced = 0;
    for (const a of this.employeeSkillAssignments) {
      if (a.proficiency <= 3) beginner++;
      else if (a.proficiency <= 7) intermediate++;
      else advanced++;
    }
    const total = (beginner + intermediate + advanced) || 1;
    return [
      { label: 'Advanced', range: 'Level 8 - 10', count: advanced, percent: Math.round((advanced / total) * 100), color: '#10b981' },
      { label: 'Intermediate', range: 'Level 4 - 7', count: intermediate, percent: Math.round((intermediate / total) * 100), color: '#3b82f6' },
      { label: 'Beginner', range: 'Level 1 - 3', count: beginner, percent: Math.round((beginner / total) * 100), color: '#f59e0b' }
    ];
  }

  get monthlyAssessmentChart(): { month: string; target: number; completed: number; targetH: number; completedH: number }[] {
    const data = [
      { month: 'May', target: 80, completed: 72 },
      { month: 'Jun', target: 90, completed: 86 },
      { month: 'Jul', target: 100, completed: 94 },
      { month: 'Aug', target: 110, completed: 105 },
      { month: 'Sep', target: 120, completed: 118 },
      { month: 'Oct', target: 130, completed: 125 }
    ];
    const maxVal = 140;
    return data.map(d => ({
      ...d,
      targetH: Math.round((d.target / maxVal) * 100),
      completedH: Math.round((d.completed / maxVal) * 100)
    }));
  }
}

