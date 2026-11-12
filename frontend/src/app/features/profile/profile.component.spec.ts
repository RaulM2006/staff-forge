import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile.component';
import { ProfileService } from '../../core/services/profile.service';
import { SkillService } from '../../core/services/skill.service';
import { EmployeeSkill, Profile } from '../../core/models/profile.model';
import { Skill } from '../../core/models/skill.model';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { By } from '@angular/platform-browser';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let profileServiceSpy: jasmine.SpyObj<ProfileService>;
  let skillServiceSpy: jasmine.SpyObj<SkillService>;

  const mockProfile: Profile = {
    id: 1,
    userId: 10,
    email: 'employee@staffforge.dev',
    firstName: 'Elena',
    lastName: 'Dumitrescu',
    role: 'EMPLOYEE',
    jobTitle: 'Frontend Engineer',
    department: 'Engineering',
    seniorityLevel: 'MID',
    capacityPercentage: 90,
    bio: 'Passionate UI/UX developer.',
    createdAt: '2026-01-01T00:00:00Z',
    skills: [
      {
        id: 101,
        skillId: 1,
        skillName: 'TypeScript',
        skillCategory: 'Frontend',
        proficiencyLevel: 'ADVANCED',
        yearsOfExperience: 3,
        createdAt: '2026-01-01T00:00:00Z'
      }
    ]
  };

  const mockGlobalSkills: Skill[] = [
    { id: 1, name: 'TypeScript', category: 'Frontend' },
    { id: 2, name: 'Angular', category: 'Frontend' },
    { id: 3, name: 'PostgreSQL', category: 'Database' }
  ];

  beforeEach(async () => {
    profileServiceSpy = jasmine.createSpyObj('ProfileService', ['getMyProfile', 'addSkill', 'removeSkill']);
    skillServiceSpy = jasmine.createSpyObj('SkillService', ['getSkills']);

    profileServiceSpy.getMyProfile.and.returnValue(of(mockProfile));
    skillServiceSpy.getSkills.and.returnValue(of(mockGlobalSkills));

    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [
        { provide: ProfileService, useValue: profileServiceSpy },
        { provide: SkillService, useValue: skillServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component and load profile and skills on init', () => {
    expect(component).toBeTruthy();
    expect(profileServiceSpy.getMyProfile).toHaveBeenCalled();
    expect(skillServiceSpy.getSkills).toHaveBeenCalled();
    expect(component.isLoading()).toBeFalse();
    expect(component.profile()).toEqual(mockProfile);
    expect(component.globalSkills()).toEqual(mockGlobalSkills);
  });

  it('should display the user basic information in the DOM', () => {
    const nameEl = fixture.debugElement.query(By.css('#profile-user-name')).nativeElement;
    const emailEl = fixture.debugElement.query(By.css('#profile-email')).nativeElement;
    const roleBadge = fixture.debugElement.query(By.css('#profile-role-badge')).nativeElement;
    const seniorityBadge = fixture.debugElement.query(By.css('#profile-seniority-badge')).nativeElement;
    const bioEl = fixture.debugElement.query(By.css('#profile-bio')).nativeElement;

    expect(nameEl.textContent).toContain('Elena Dumitrescu');
    expect(emailEl.textContent).toContain('employee@staffforge.dev');
    expect(roleBadge.textContent).toContain('EMPLOYEE');
    expect(seniorityBadge.textContent).toContain('MID');
    expect(bioEl.textContent).toContain('Passionate UI/UX developer.');
  });

  it('should render the list of current employee skills with proficiency and experience', () => {
    const cards = fixture.debugElement.queryAll(By.css('[data-skill-card-id]'));
    expect(cards.length).toBe(1);

    const firstCard = cards[0].nativeElement;
    expect(firstCard.textContent).toContain('TypeScript');
    expect(firstCard.textContent).toContain('ADVANCED');
    expect(firstCard.textContent).toContain('3 yrs exp');
  });

  it('should correctly identify which global skills are already associated', () => {
    const available = component.availableSkills();
    expect(available.length).toBe(3);

    const tsSkill = available.find((s) => s.id === 1);
    const angularSkill = available.find((s) => s.id === 2);

    expect(tsSkill?.isAlreadyAdded).toBeTrue();
    expect(angularSkill?.isAlreadyAdded).toBeFalse();
  });

  describe('Add Skill Form & Dynamic State Updates', () => {
    it('should validate required fields in addSkillForm', () => {
      component.addSkillForm.setValue({
        skillId: '',
        proficiencyLevel: 'INTERMEDIATE',
        yearsOfExperience: 1
      });

      component.onAddSkill();
      expect(component.skillIdControl?.invalid).toBeTrue();
      expect(profileServiceSpy.addSkill).not.toHaveBeenCalled();
    });

    it('should validate non-negative years of experience', () => {
      component.addSkillForm.patchValue({
        skillId: '2',
        yearsOfExperience: -1
      });

      component.onAddSkill();
      expect(component.yearsOfExperienceControl?.invalid).toBeTrue();
      expect(profileServiceSpy.addSkill).not.toHaveBeenCalled();
    });

    it('should successfully add skill and dynamically update the profile skills list', () => {
      const newSkill: EmployeeSkill = {
        id: 102,
        skillId: 2,
        skillName: 'Angular',
        skillCategory: 'Frontend',
        proficiencyLevel: 'EXPERT',
        yearsOfExperience: 4,
        createdAt: '2026-01-02T00:00:00Z'
      };

      profileServiceSpy.addSkill.and.returnValue(of(newSkill));

      component.addSkillForm.setValue({
        skillId: '2',
        proficiencyLevel: 'EXPERT',
        yearsOfExperience: 4
      });

      component.onAddSkill();
      fixture.detectChanges();

      expect(profileServiceSpy.addSkill).toHaveBeenCalledWith({
        skillId: 2,
        proficiencyLevel: 'EXPERT',
        yearsOfExperience: 4
      });

      // Verify dynamic update in signal
      expect(component.profile()?.skills.length).toBe(2);
      expect(component.profile()?.skills.some((s) => s.skillId === 2)).toBeTrue();

      // Verify DOM update
      const updatedCards = fixture.debugElement.queryAll(By.css('[data-skill-card-id]'));
      expect(updatedCards.length).toBe(2);

      // Verify form reset
      expect(component.skillIdControl?.value).toBe('');
      expect(component.notification()?.type).toBe('success');
      expect(component.notification()?.message).toContain('Angular');
    });

    it('should display error message on 409 conflict when skill already associated', () => {
      const conflictError = new HttpErrorResponse({
        status: 409,
        statusText: 'Conflict',
        error: { message: 'Skill "TypeScript" is already associated with this profile' }
      });

      profileServiceSpy.addSkill.and.returnValue(throwError(() => conflictError));

      component.addSkillForm.setValue({
        skillId: '1',
        proficiencyLevel: 'INTERMEDIATE',
        yearsOfExperience: 2
      });

      component.onAddSkill();
      fixture.detectChanges();

      expect(component.isAddingSkill()).toBeFalse();
      expect(component.addSkillError()).toContain('already associated');

      const alert = fixture.debugElement.query(By.css('#add-skill-error-alert'));
      expect(alert).toBeTruthy();
      expect(alert.nativeElement.textContent).toContain('already associated');
    });
  });

  describe('Remove Skill Flow & Dynamic State Updates', () => {
    it('should call removeSkill and dynamically remove the skill from profile state and DOM', () => {
      profileServiceSpy.removeSkill.and.returnValue(of(undefined as unknown as void));

      const skillToRemove = mockProfile.skills[0];
      component.onRemoveSkill(skillToRemove);
      fixture.detectChanges();

      expect(profileServiceSpy.removeSkill).toHaveBeenCalledWith(skillToRemove.skillId);
      expect(component.profile()?.skills.length).toBe(0);

      // Check empty state message appears dynamically
      const emptyState = fixture.debugElement.query(By.css('#no-skills-message'));
      expect(emptyState).toBeTruthy();
      expect(component.notification()?.type).toBe('success');
      expect(component.notification()?.message).toContain('TypeScript');
    });

    it('should show error notification when skill removal fails', () => {
      profileServiceSpy.removeSkill.and.returnValue(throwError(() => new Error('Server error')));

      const skillToRemove = mockProfile.skills[0];
      component.onRemoveSkill(skillToRemove);
      fixture.detectChanges();

      expect(component.profile()?.skills.length).toBe(1);
      expect(component.notification()?.type).toBe('error');
      expect(component.notification()?.message).toContain('Failed to remove');
    });
  });

  describe('Error handling during loading', () => {
    it('should display error banner if getMyProfile fails', () => {
      profileServiceSpy.getMyProfile.and.returnValue(throwError(() => new Error('Failed to load')));
      component.loadProfileAndCatalog();
      fixture.detectChanges();

      expect(component.isLoading()).toBeFalse();
      expect(component.errorMessage()).toContain('Failed to load profile');

      const errorAlert = fixture.debugElement.query(By.css('[role="alert"]'));
      expect(errorAlert).toBeTruthy();
      expect(errorAlert.nativeElement.textContent).toContain('Failed to load profile');
    });
  });
});
