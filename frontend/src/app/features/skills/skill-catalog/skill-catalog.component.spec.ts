import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { SkillCatalogComponent } from './skill-catalog.component';
import { SkillService } from '../../../core/services/skill.service';
import { Skill } from '../../../core/models/skill.model';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { By } from '@angular/platform-browser';

describe('SkillCatalogComponent', () => {
  let component: SkillCatalogComponent;
  let fixture: ComponentFixture<SkillCatalogComponent>;
  let skillServiceSpy: jasmine.SpyObj<SkillService>;

  const initialSkills: Skill[] = [
    { id: 1, name: 'Java', category: 'Backend', description: 'Core Java programming', createdAt: '2026-01-01T10:00:00Z' },
    { id: 2, name: 'Angular', category: 'Frontend', description: 'Web framework', createdAt: '2026-01-02T10:00:00Z' },
    { id: 3, name: 'Docker', category: 'DevOps', description: 'Containerization tool', createdAt: '2026-01-03T10:00:00Z' }
  ];

  beforeEach(async () => {
    skillServiceSpy = jasmine.createSpyObj('SkillService', ['getSkills', 'createSkill', 'deleteSkill']);
    skillServiceSpy.getSkills.and.returnValue(of(initialSkills));

    await TestBed.configureTestingModule({
      imports: [SkillCatalogComponent],
      providers: [
        { provide: SkillService, useValue: skillServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SkillCatalogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component and fetch skills on init', () => {
    expect(component).toBeTruthy();
    expect(skillServiceSpy.getSkills).toHaveBeenCalled();
    expect(component.skills().length).toBe(3);
    expect(component.isLoading()).toBeFalse();
    expect(component.errorMessage()).toBeNull();
  });

  it('should render table rows matching fetched skills', () => {
    const rows = fixture.debugElement.queryAll(By.css('#skills-table tbody tr'));
    expect(rows.length).toBe(3);

    const firstRowText = rows[0].nativeElement.textContent;
    expect(firstRowText).toContain('Java');
    expect(firstRowText).toContain('Backend');
  });

  describe('Error handling during loading', () => {
    it('should display error message when loading fails', () => {
      skillServiceSpy.getSkills.and.returnValue(throwError(() => new Error('Server error')));
      component.loadSkills();
      fixture.detectChanges();

      expect(component.isLoading()).toBeFalse();
      expect(component.errorMessage()).toContain('Failed to load skills');

      const errorAlert = fixture.debugElement.query(By.css('[role="alert"]'));
      expect(errorAlert).toBeTruthy();
      expect(errorAlert.nativeElement.textContent).toContain('Failed to load skills');
    });

    it('should retry loading skills when retry button is clicked', () => {
      component.errorMessage.set('Failed to load skills');
      fixture.detectChanges();

      skillServiceSpy.getSkills.and.returnValue(of(initialSkills));
      const retryBtn = fixture.debugElement.query(By.css('[role="alert"] button'));
      retryBtn.nativeElement.click();

      expect(skillServiceSpy.getSkills).toHaveBeenCalledTimes(2);
      expect(component.skills().length).toBe(3);
    });
  });

  describe('Search & Filtering', () => {
    it('should filter skills by name', () => {
      component.searchQuery.set('angular');
      fixture.detectChanges();

      expect(component.filteredSkills().length).toBe(1);
      expect(component.filteredSkills()[0].name).toBe('Angular');
    });

    it('should filter skills by category', () => {
      component.searchQuery.set('devops');
      fixture.detectChanges();

      expect(component.filteredSkills().length).toBe(1);
      expect(component.filteredSkills()[0].name).toBe('Docker');
    });

    it('should filter skills by description', () => {
      component.searchQuery.set('containerization');
      fixture.detectChanges();

      expect(component.filteredSkills().length).toBe(1);
      expect(component.filteredSkills()[0].name).toBe('Docker');
    });

    it('should show empty state when search finds no matches', () => {
      component.searchQuery.set('non-existent-skill-xyz');
      fixture.detectChanges();

      expect(component.filteredSkills().length).toBe(0);
      const emptyState = fixture.debugElement.query(By.css('h3'));
      expect(emptyState.nativeElement.textContent).toContain('No matching skills found');
    });
  });

  describe('Add Skill Modal & Form Validation', () => {
    it('should open and close the modal', () => {
      expect(component.isAddModalOpen()).toBeFalse();

      component.openAddModal();
      expect(component.isAddModalOpen()).toBeTrue();

      component.closeAddModal();
      expect(component.isAddModalOpen()).toBeFalse();
    });

    it('should validate skill name as required', () => {
      component.openAddModal();
      const nameControl = component.nameControl;
      expect(nameControl?.valid).toBeFalse();
      expect(nameControl?.errors?.['required']).toBeTrue();

      nameControl?.setValue('PostgreSQL');
      expect(nameControl?.valid).toBeTrue();
    });

    it('should validate skill name max length (100 characters)', () => {
      component.openAddModal();
      const nameControl = component.nameControl;
      nameControl?.setValue('a'.repeat(101));
      expect(nameControl?.valid).toBeFalse();
      expect(nameControl?.errors?.['maxlength']).toBeTruthy();

      nameControl?.setValue('a'.repeat(100));
      expect(nameControl?.valid).toBeTrue();
    });

    it('should validate category max length (50 characters)', () => {
      component.openAddModal();
      const categoryControl = component.categoryControl;
      categoryControl?.setValue('c'.repeat(51));
      expect(categoryControl?.valid).toBeFalse();
      expect(categoryControl?.errors?.['maxlength']).toBeTruthy();

      categoryControl?.setValue('c'.repeat(50));
      expect(categoryControl?.valid).toBeTrue();
    });

    it('should mark form controls touched and not call service when form is invalid', () => {
      component.openAddModal();
      component.onSaveSkill();

      expect(component.addSkillForm.touched).toBeTrue();
      expect(skillServiceSpy.createSkill).not.toHaveBeenCalled();
    });

    it('should successfully create skill and append to the list', () => {
      const createdSkill: Skill = {
        id: 4,
        name: 'Kubernetes',
        category: 'DevOps',
        description: 'Orchestration',
        createdAt: '2026-01-04T00:00:00Z'
      };
      skillServiceSpy.createSkill.and.returnValue(of(createdSkill));

      component.openAddModal();
      component.addSkillForm.setValue({
        name: 'Kubernetes',
        category: 'DevOps',
        description: 'Orchestration'
      });

      component.onSaveSkill();

      expect(skillServiceSpy.createSkill).toHaveBeenCalledWith({
        name: 'Kubernetes',
        category: 'DevOps',
        description: 'Orchestration'
      });
      expect(component.skills().length).toBe(4);
      expect(component.isAddModalOpen()).toBeFalse();
      expect(component.notification()?.type).toBe('success');
      expect(component.notification()?.message).toContain('Kubernetes');
    });

    it('should handle 409 conflict when skill name already exists', () => {
      const conflictError = new HttpErrorResponse({
        status: 409,
        statusText: 'Conflict',
        error: { message: 'Skill with name "Java" already exists' }
      });
      skillServiceSpy.createSkill.and.returnValue(throwError(() => conflictError));

      component.openAddModal();
      component.addSkillForm.setValue({
        name: 'Java',
        category: 'Backend',
        description: 'Duplicate'
      });

      component.onSaveSkill();

      expect(component.isAddModalOpen()).toBeTrue();
      expect(component.modalErrorMessage()).toContain('already exists');
    });
  });

  describe('Delete Skill Flow', () => {
    it('should open delete confirmation dialog with selected skill', () => {
      expect(component.skillToDelete()).toBeNull();

      component.confirmDelete(initialSkills[0]);
      expect(component.skillToDelete()).toEqual(initialSkills[0]);
    });

    it('should cancel delete dialog', () => {
      component.confirmDelete(initialSkills[0]);
      component.cancelDelete();
      expect(component.skillToDelete()).toBeNull();
    });

    it('should successfully delete skill and remove from list', () => {
      skillServiceSpy.deleteSkill.and.returnValue(of(undefined as unknown as void));

      component.confirmDelete(initialSkills[0]);
      component.executeDelete();

      expect(skillServiceSpy.deleteSkill).toHaveBeenCalledWith(1);
      expect(component.skills().length).toBe(2);
      expect(component.skills().find((s) => s.id === 1)).toBeUndefined();
      expect(component.skillToDelete()).toBeNull();
      expect(component.notification()?.type).toBe('success');
    });

    it('should handle error when delete fails', () => {
      skillServiceSpy.deleteSkill.and.returnValue(throwError(() => new Error('Forbidden')));

      component.confirmDelete(initialSkills[0]);
      component.executeDelete();

      expect(component.skills().length).toBe(3);
      expect(component.skillToDelete()).toBeNull();
      expect(component.notification()?.type).toBe('error');
      expect(component.notification()?.message).toContain('Failed to delete');
    });
  });
});
