export type ProficiencyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export interface EmployeeSkill {
  id: number;
  skillId: number;
  skillName: string;
  skillCategory?: string | null;
  proficiencyLevel: ProficiencyLevel;
  yearsOfExperience: number;
  createdAt?: string | null;
}

export interface Profile {
  id: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  jobTitle?: string | null;
  department?: string | null;
  seniorityLevel?: string | null;
  capacityPercentage?: number | null;
  bio?: string | null;
  createdAt?: string | null;
  skills: EmployeeSkill[];
}

export interface AddSkillToProfileRequest {
  skillId: number;
  proficiencyLevel: ProficiencyLevel;
  yearsOfExperience: number;
}
