import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ProfileService } from './profile.service';
import { AddSkillToProfileRequest, EmployeeSkill, Profile } from '../models/profile.model';

describe('ProfileService', () => {
  let service: ProfileService;
  let httpTesting: HttpTestingController;

  const mockProfile: Profile = {
    id: 1,
    userId: 1,
    email: 'employee@staffforge.dev',
    firstName: 'Ion',
    lastName: 'Popescu',
    role: 'EMPLOYEE',
    jobTitle: 'Backend Developer',
    department: 'Engineering',
    seniorityLevel: 'MID',
    capacityPercentage: 90,
    bio: 'Software engineer building web APIs.',
    createdAt: '2026-01-01T00:00:00Z',
    skills: [
      {
        id: 10,
        skillId: 1,
        skillName: 'Java',
        skillCategory: 'Backend',
        proficiencyLevel: 'ADVANCED',
        yearsOfExperience: 3,
        createdAt: '2026-01-01T00:00:00Z'
      }
    ]
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProfileService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ProfileService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch the authenticated user profile via GET /api/profiles/me', () => {
    service.getMyProfile().subscribe((profile) => {
      expect(profile).toEqual(mockProfile);
      expect(profile.skills.length).toBe(1);
    });

    const req = httpTesting.expectOne('/api/profiles/me');
    expect(req.request.method).toBe('GET');
    req.flush(mockProfile);
  });

  it('should associate a skill via POST /api/profiles/me/skills', () => {
    const payload: AddSkillToProfileRequest = {
      skillId: 2,
      proficiencyLevel: 'EXPERT',
      yearsOfExperience: 5
    };

    const mockResponse: EmployeeSkill = {
      id: 11,
      skillId: 2,
      skillName: 'Angular',
      skillCategory: 'Frontend',
      proficiencyLevel: 'EXPERT',
      yearsOfExperience: 5,
      createdAt: '2026-01-02T00:00:00Z'
    };

    service.addSkill(payload).subscribe((added) => {
      expect(added).toEqual(mockResponse);
    });

    const req = httpTesting.expectOne('/api/profiles/me/skills');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(mockResponse);
  });

  it('should remove a skill via DELETE /api/profiles/me/skills/{skillId}', () => {
    service.removeSkill(1).subscribe((res) => {
      expect(res).toBeNull();
    });

    const req = httpTesting.expectOne('/api/profiles/me/skills/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
