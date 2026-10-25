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
  employee?: { id?: number; name?: string; email?: string };
}
