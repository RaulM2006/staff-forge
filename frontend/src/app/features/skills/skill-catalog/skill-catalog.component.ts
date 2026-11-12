import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { SkillService } from '../../../core/services/skill.service';
import { CreateSkillRequest, Skill } from '../../../core/models/skill.model';

@Component({
  selector: 'app-skill-catalog',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, DatePipe],
  templateUrl: './skill-catalog.component.html',
  styleUrl: './skill-catalog.component.scss'
})
export class SkillCatalogComponent implements OnInit {
  private readonly skillService = inject(SkillService);
  private readonly fb = inject(FormBuilder);

  readonly skills = signal<Skill[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly notification = signal<{ type: 'success' | 'error'; message: string } | null>(null);
  readonly searchQuery = signal('');

  // Add Skill Modal State
  readonly isAddModalOpen = signal(false);
  readonly isSubmitting = signal(false);
  readonly modalErrorMessage = signal<string | null>(null);

  // Delete Confirmation State
  readonly skillToDelete = signal<Skill | null>(null);
  readonly isDeleting = signal(false);

  readonly addSkillForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    category: ['', [Validators.maxLength(50)]],
    description: ['']
  });

  readonly filteredSkills = computed(() => {
    const query = this.searchQuery().trim().toLowerCase();
    const list = this.skills();

    if (!query) {
      return list;
    }

    return list.filter((skill) =>
      skill.name.toLowerCase().includes(query) ||
      (skill.category && skill.category.toLowerCase().includes(query)) ||
      (skill.description && skill.description.toLowerCase().includes(query))
    );
  });

  get nameControl() {
    return this.addSkillForm.get('name');
  }

  get categoryControl() {
    return this.addSkillForm.get('category');
  }

  ngOnInit(): void {
    this.loadSkills();
  }

  loadSkills(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.skillService.getSkills().subscribe({
      next: (data) => {
        this.skills.set(data);
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.errorMessage.set('Failed to load skills from server. Please try again.');
      }
    });
  }

  openAddModal(): void {
    this.addSkillForm.reset();
    this.modalErrorMessage.set(null);
    this.isAddModalOpen.set(true);
  }

  closeAddModal(): void {
    if (this.isSubmitting()) return;
    this.isAddModalOpen.set(false);
    this.modalErrorMessage.set(null);
    this.addSkillForm.reset();
  }

  onSaveSkill(): void {
    if (this.addSkillForm.invalid) {
      this.addSkillForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.modalErrorMessage.set(null);

    const { name, category, description } = this.addSkillForm.getRawValue();
    const payload: CreateSkillRequest = {
      name: (name ?? '').trim(),
      category: category?.trim() ? category.trim() : null,
      description: description?.trim() ? description.trim() : null
    };

    this.skillService.createSkill(payload).subscribe({
      next: (created) => {
        this.skills.update((current) => [...current, created]);
        this.isSubmitting.set(false);
        this.isAddModalOpen.set(false);
        this.showNotification('success', `Skill "${created.name}" created successfully.`);
      },
      error: (error: unknown) => {
        this.isSubmitting.set(false);

        if (error instanceof HttpErrorResponse) {
          if (error.status === 409) {
            this.modalErrorMessage.set(
              error.error?.message || `A skill named "${payload.name}" already exists.`
            );
          } else if (error.error?.message && typeof error.error.message === 'string') {
            this.modalErrorMessage.set(error.error.message);
          } else {
            this.modalErrorMessage.set('Failed to create skill. Please try again.');
          }
        } else {
          this.modalErrorMessage.set('An unexpected error occurred. Please try again.');
        }
      }
    });
  }

  confirmDelete(skill: Skill): void {
    this.skillToDelete.set(skill);
  }

  cancelDelete(): void {
    if (this.isDeleting()) return;
    this.skillToDelete.set(null);
  }

  executeDelete(): void {
    const target = this.skillToDelete();
    if (!target) return;

    this.isDeleting.set(true);

    this.skillService.deleteSkill(target.id).subscribe({
      next: () => {
        this.skills.update((current) => current.filter((s) => s.id !== target.id));
        this.isDeleting.set(false);
        this.skillToDelete.set(null);
        this.showNotification('success', `Skill "${target.name}" was deleted successfully.`);
      },
      error: (error: unknown) => {
        this.isDeleting.set(false);
        this.skillToDelete.set(null);
        this.showNotification('error', `Failed to delete skill "${target.name}".`);
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
    }, 5000);
  }
}
