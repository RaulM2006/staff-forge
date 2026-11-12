import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SkillService } from './skill.service';
import { CreateSkillRequest, Skill } from '../models/skill.model';

describe('SkillService', () => {
  let service: SkillService;
  let httpTesting: HttpTestingController;

  const mockSkills: Skill[] = [
    { id: 1, name: 'Java', category: 'Backend', description: 'Core Java', createdAt: '2026-01-01T00:00:00Z' },
    { id: 2, name: 'Angular', category: 'Frontend', description: 'Single Page App framework', createdAt: '2026-01-02T00:00:00Z' }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SkillService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(SkillService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all skills via GET /api/skills', () => {
    service.getSkills().subscribe((skills) => {
      expect(skills.length).toBe(2);
      expect(skills).toEqual(mockSkills);
    });

    const req = httpTesting.expectOne('/api/skills');
    expect(req.request.method).toBe('GET');
    req.flush(mockSkills);
  });

  it('should create a skill via POST /api/skills', () => {
    const newSkillPayload: CreateSkillRequest = {
      name: 'Docker',
      category: 'DevOps',
      description: 'Containerization engine'
    };

    const createdSkill: Skill = {
      id: 3,
      ...newSkillPayload,
      createdAt: '2026-01-03T00:00:00Z'
    };

    service.createSkill(newSkillPayload).subscribe((skill) => {
      expect(skill).toEqual(createdSkill);
    });

    const req = httpTesting.expectOne('/api/skills');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newSkillPayload);
    req.flush(createdSkill);
  });

  it('should delete a skill via DELETE /api/skills/{id}', () => {
    service.deleteSkill(1).subscribe((response) => {
      expect(response).toBeNull();
    });

    const req = httpTesting.expectOne('/api/skills/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
