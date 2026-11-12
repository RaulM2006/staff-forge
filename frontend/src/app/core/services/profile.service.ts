import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AddSkillToProfileRequest, EmployeeSkill, Profile } from '../models/profile.model';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/profiles';

  getMyProfile(): Observable<Profile> {
    return this.http.get<Profile>(`${this.baseUrl}/me`);
  }

  addSkill(payload: AddSkillToProfileRequest): Observable<EmployeeSkill> {
    return this.http.post<EmployeeSkill>(`${this.baseUrl}/me/skills`, payload);
  }

  removeSkill(skillId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/me/skills/${skillId}`);
  }
}
