import { Skill } from './skill';

export interface EmployeeSkillAssignment {
  id?: number;
  employeeId?: number;
  studentId?: number;
  skillId?: number;
  proficiency: number;
  verificationStatus?: string;
  assessmentDate?: string;
  employee?: { id?: number; name?: string; email?: string };
  student?: { id?: number; name?: string; email?: string };
  skill?: Skill;
}

export interface Assessment {
  id?: number;
  employeeId?: number;
  studentId?: number;
  skillId?: number;
  score: number;
  maxScore: number;
  assessmentDate: string;
  assessmentMethod: string;
  verificationStatus?: string;
  assessorName?: string;
  courseId?: number;
  courseTitle?: string;
  employee?: { id?: number; name?: string; email?: string };
  student?: { id?: number; name?: string; email?: string };
  skill?: Skill;
}

export interface FrameworkSkillRequirement {
  id?: number;
  skillId?: number;
  requiredProficiency: number;
  notes?: string;
  skill?: Skill;
}

export interface CompetencyFramework {
  id?: number;
  name: string;
  roleTitle: string;
  description?: string;
  skillRequirements?: FrameworkSkillRequirement[];
}

export interface CompetencyGap {
  skillId: number;
  skillName: string;
  requiredProficiency: number;
  employeeProficiency?: number | null;
  gap: number;
  status: string;
  evidenceStatus: string;
  recommendation: string;
}

export interface CompetencyGapReport {
  frameworkId: number;
  frameworkName: string;
  roleTitle: string;
  requiredSkills: number;
  satisfiedRequirements: number;
  missingSkills: number;
  unverifiedSkills: number;
  proficiencyGaps: number;
  readinessPercentage: number;
  readinessFormula: string;
  skills: CompetencyGap[];
}

export interface Student {
  id: number;
  name: string;
  email: string;
  program: string;
}

export interface IssuedCertificate {
  id: number;
  certificateNumber: string;
  learnerName: string;
  skillName: string;
  issuerName: string;
  issueDate: string;
  status: 'ISSUED' | 'EXPIRED' | 'REVOKED';
  proficiencyLevel: number;
  verificationToken: string;
  assessmentScore: number;
  assessmentMaxScore: number;
}

export interface Certification {
  id?: number;
  employeeId?: number;
  name: string;
  issuingOrganization: string;
  credentialId?: string;
  issueDate: string;
  expiryDate?: string | null;
  verificationStatus?: string;
  status?: string;
  documentName?: string;
  documentType?: string;
  documentData?: string;
  authenticityStatus?: string;
  authenticityScore?: number;
  verificationHash?: string;
  employee?: { id?: number; name?: string; email?: string };
}

export interface CertificateAuthenticityCheck {
  name: string;
  passed: boolean;
  details: string;
}

export interface CertificateAuthenticityResponse {
  authentic: boolean;
  confidenceScore: number;
  status: string;
  issuer: string;
  credentialId: string;
  verificationHash: string;
  digitalSignatureValid: boolean;
  tamperEvidence: string;
  checks: CertificateAuthenticityCheck[];
  verificationBadge: string;
  message: string;
}

export interface Course {
  id?: number;
  title: string;
  description: string;
  durationHours: number;
  instructor: string;
  courseType: string;
  rating: number;
  totalEnrolled: number;
  totalCompleted: number;
  category: string;
}

export interface CourseEnrollment {
  id?: number;
  course: Course;
  employeeId?: number;
  studentId?: number;
  status: string;
  progressPercentage: number;
  enrolledAt: string;
  completedAt?: string;
}

export interface LearningPath {
  id?: number;
  title: string;
  description: string;
  targetRole: string;
  totalCourses: number;
  progressPercentage: number;
  coursesSummary: string;
}

export interface CareerPlan {
  id?: number;
  employeeId?: number;
  studentId?: number;
  currentRole: string;
  targetRole: string;
  progressPercentage: number;
  skillGaps: string;
  mentorName: string;
  promotionReadinessScore: number;
  promotionEligibility: string;
  status: string;
}

export interface InternalJob {
  id?: number;
  title: string;
  department: string;
  description: string;
  requiredSkills: string;
  openingsCount: number;
  status: string;
}

export interface JobApplication {
  id?: number;
  job: InternalJob;
  employeeId?: number;
  studentId?: number;
  appliedAt: string;
  matchPercentage: number;
  status: string;
}

export interface ScorecardEmail {
  id: number;
  attemptId: number;
  assessmentId: number;
  assessmentTitle: string;
  studentId?: number;
  employeeId?: number;
  recipientEmail: string;
  recipientName?: string;
  scorePoints: number;
  maxPoints: number;
  percentage: number;
  passed: boolean;
  subject: string;
  bodyText: string;
  status: string;
  sentAt: string;
}

export interface AiGenerateQuestionsRequest {
  courseId: number;
  courseTitle?: string;
  courseDescription?: string;
  courseCategory?: string;
  questionCount: number;
}

export interface CreateAssessmentWithQuestionsRequest {
  title: string;
  instructions: string;
  skillId: number;
  courseId?: number;
  courseTitle?: string;
  passPercentage: number;
  maxAttempts: number;
  active?: boolean;
  questions: Array<{
    prompt: string;
    options: string[];
    correctOptionIndex: number;
    difficulty: string;
    points: number;
  }>;
}
