import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ProfileService } from '../../core/services/profile.service';
import { SkillService } from '../../core/services/skill.service';
import { EmployeeSkill, ProficiencyLevel, Profile } from '../../core/models/profile.model';
import { Skill } from '../../core/models/skill.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  private readonly profileService = inject(ProfileService);
  private readonly skillService = inject(SkillService);
  private readonly fb = inject(FormBuilder);

  readonly profile = signal<Profile | null>(null);
  readonly globalSkills = signal<Skill[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly isAddingSkill = signal(false);
  readonly addSkillError = signal<string | null>(null);
  readonly removingSkillId = signal<number | null>(null);
  readonly notification = signal<{ type: 'success' | 'error'; message: string } | null>(null);

  readonly proficiencyLevels: { value: ProficiencyLevel; label: string; description: string }[] = [
    { value: 'BEGINNER', label: 'Beginner', description: 'Basic understanding and limited practical experience' },
    { value: 'INTERMEDIATE', label: 'Intermediate', description: 'Practical application and autonomous working knowledge' },
    { value: 'ADVANCED', label: 'Advanced', description: 'Deep technical competence and problem-solving ability' },
    { value: 'EXPERT', label: 'Expert', description: 'Mastery, authority, and ability to mentor others' }
  ];

  readonly addSkillForm = this.fb.group({
    skillId: ['', [Validators.required]],
    proficiencyLevel: ['INTERMEDIATE' as ProficiencyLevel, [Validators.required]],
    yearsOfExperience: [1, [Validators.required, Validators.min(0), Validators.max(50)]]
  });

  readonly userInitials = computed(() => {
    const p = this.profile();
    if (!p) return '??';
    const first = p.firstName ? p.firstName.charAt(0) : '';
    const last = p.lastName ? p.lastName.charAt(0) : '';
    return `${first}${last}`.toUpperCase() || 'U';
  });

  readonly associatedSkillIds = computed(() => {
    const p = this.profile();
    if (!p || !p.skills) return new Set<number>();
    return new Set<number>(p.skills.map((s) => s.skillId));
  });

  readonly availableSkills = computed(() => {
    const existing = this.associatedSkillIds();
    return this.globalSkills().map((skill) => ({
      ...skill,
      isAlreadyAdded: existing.has(skill.id)
    }));
  });

  get skillIdControl() {
    return this.addSkillForm.get('skillId');
  }

  get proficiencyLevelControl() {
    return this.addSkillForm.get('proficiencyLevel');
  }

  get yearsOfExperienceControl() {
    return this.addSkillForm.get('yearsOfExperience');
  }

  ngOnInit(): void {
    this.loadProfileAndCatalog();
  }

  loadProfileAndCatalog(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    let profileLoaded = false;
    let catalogLoaded = false;

    const checkDone = () => {
      if (profileLoaded && catalogLoaded) {
        this.isLoading.set(false);
      }
    };

    this.profileService.getMyProfile().subscribe({
      next: (data) => {
        this.profile.set(data);
        profileLoaded = true;
        checkDone();
      },
      error: (error: unknown) => {
        profileLoaded = true;
        this.errorMessage.set('Failed to load profile details. Please try again.');
        this.isLoading.set(false);
      }
    });

    this.skillService.getSkills().subscribe({
      next: (skills) => {
        this.globalSkills.set(skills);
        catalogLoaded = true;
        checkDone();
      },
      error: () => {
        catalogLoaded = true;
        checkDone();
      }
    });
  }

  onAddSkill(): void {
    if (this.addSkillForm.invalid) {
      this.addSkillForm.markAllAsTouched();
      return;
    }

    this.isAddingSkill.set(true);
    this.addSkillError.set(null);

    const { skillId, proficiencyLevel, yearsOfExperience } = this.addSkillForm.getRawValue();

    const selectedSkillId = Number(skillId);
    const selectedProficiency = (proficiencyLevel ?? 'INTERMEDIATE') as ProficiencyLevel;
    const selectedYears = Number(yearsOfExperience ?? 0);

    this.profileService.addSkill({
      skillId: selectedSkillId,
      proficiencyLevel: selectedProficiency,
      yearsOfExperience: selectedYears
    }).subscribe({
      next: (newSkill) => {
        this.profile.update((current) => {
          if (!current) return current;
          return {
            ...current,
            skills: [...current.skills, newSkill]
          };
        });

        this.isAddingSkill.set(false);
        this.addSkillForm.reset({
          skillId: '',
          proficiencyLevel: 'INTERMEDIATE',
          yearsOfExperience: 1
        });
        this.showNotification('success', `Added "${newSkill.skillName}" to your profile.`);
      },
      error: (error: unknown) => {
        this.isAddingSkill.set(false);

        if (error instanceof HttpErrorResponse) {
          if (error.status === 409) {
            this.addSkillError.set(
              error.error?.message || 'This skill is already associated with your profile.'
            );
          } else if (error.error?.message && typeof error.error.message === 'string') {
            this.addSkillError.set(error.error.message);
          } else {
            this.addSkillError.set('Failed to add skill. Please try again.');
          }
        } else {
          this.addSkillError.set('An unexpected error occurred. Please try again.');
        }
      }
    });
  }

  onRemoveSkill(skill: EmployeeSkill): void {
    if (this.removingSkillId() !== null) return;

    this.removingSkillId.set(skill.skillId);

    this.profileService.removeSkill(skill.skillId).subscribe({
      next: () => {
        this.profile.update((current) => {
          if (!current) return current;
          return {
            ...current,
            skills: current.skills.filter((s) => s.skillId !== skill.skillId)
          };
        });

        this.removingSkillId.set(null);
        this.showNotification('success', `Removed "${skill.skillName}" from your profile.`);
      },
      error: (error: unknown) => {
        this.removingSkillId.set(null);
        this.showNotification('error', `Failed to remove "${skill.skillName}". Please try again.`);
      }
    });
  }

  dismissNotification(): void {
    this.notification.set(null);
  }

  private showNotification(type: 'success' | 'error', message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => {
      if (this.notification()?.message === message) {
        this.notification.set(null);
      }
    }, 4500);
  }
}
