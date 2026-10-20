
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
import {
  Assessment,
  Certification,
  CompetencyFramework,
  CompetencyGap,
  EmployeeSkillAssignment,
  FrameworkSkillRequirement
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
    | 'certifications' = 'dashboard';

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
  certificationLoading = false;
  certificationError = '';
  certificationSuccess = '';
  showCertificationForm = false;
  editingCertificationId: number | null = null;
  certification: Certification = this.emptyCertification();

  constructor(
    private http: HttpClient,
    private skillService: SkillService,
    private employeeSkillService: EmployeeSkillService,
    private assessmentService: AssessmentService,
    private frameworkService: FrameworkService,
    private certificationService: CertificationService,
    @Inject(AuthService) public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
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
  }

  get isHrAdmin(): boolean {
    return this.authService.isHrAdmin;
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login');
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
  }

  openAddCertificationForm(): void {
    this.editingCertificationId = null;
    this.certification = this.emptyCertification();
    this.showCertificationForm = true;
    this.certificationError = '';
    this.certificationSuccess = '';
  }

  openEditCertificationForm(item: Certification): void {
    this.editingCertificationId = item.id ?? null;
    this.certification = {
      employeeId: item.employeeId ?? item.employee?.id,
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
        return 'status-pill danger';
      default:
        return 'status-pill neutral';
    }
  }
}
