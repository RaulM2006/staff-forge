export interface Skill {
  id: number;
  name: string;
  category?: string | null;
  description?: string | null;
  createdAt?: string | null;
}

export interface CreateSkillRequest {
  name: string;
  category?: string | null;
  description?: string | null;
}
