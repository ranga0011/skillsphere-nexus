import { Skill } from './skill';

export interface EmployeeSkillAssignment {
  id?: number;
  employeeId?: number;
  skillId?: number;
  proficiency: number;
  verificationStatus?: string;
  assessmentDate?: string;
  employee?: { id?: number; name?: string; email?: string };
  skill?: Skill;
}

export interface Assessment {
  id?: number;
  employeeId?: number;
  skillId?: number;
  score: number;
  maxScore: number;
  assessmentDate: string;
  assessmentMethod: string;
  verificationStatus?: string;
  assessorName?: string;
  employee?: { id?: number; name?: string; email?: string };
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
