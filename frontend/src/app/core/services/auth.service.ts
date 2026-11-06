import { Injectable, signal, computed } from '@angular/core';
import { Role, User } from '../models/user.model';

const MOCK_USERS_BY_ROLE: Record<Role, { firstName: string; lastName: string; email: string }> = {
  [Role.ADMIN]: { firstName: 'Admin', lastName: 'User', email: 'admin@staffforge.dev' },
  [Role.EMPLOYEE]: { firstName: 'Ion', lastName: 'Popescu', email: 'employee@staffforge.dev' },
  [Role.PROJECT_MANAGER]: { firstName: 'Maria', lastName: 'Ionescu', email: 'pm@staffforge.dev' },
  [Role.RESOURCE_MANAGER]: { firstName: 'Andrei', lastName: 'Radu', email: 'rm@staffforge.dev' },
  [Role.HR]: { firstName: 'Elena', lastName: 'Dumitrescu', email: 'hr@staffforge.dev' }
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly userSignal = signal<User | null>({
    id: 1,
    ...MOCK_USERS_BY_ROLE[Role.ADMIN],
    role: Role.ADMIN
  });

  /** The currently authenticated user, or `null` if not logged in. */
  readonly currentUser = this.userSignal.asReadonly();

  /** Whether a user is currently authenticated. */
  readonly isAuthenticated = computed(() => this.currentUser() !== null);

  /**
   * Switches the mock user's role and identity — useful for testing the role-based UI.
   */
  setMockRole(role: Role): void {
    const info = MOCK_USERS_BY_ROLE[role];
    this.userSignal.set({
      id: 1,
      email: info.email,
      firstName: info.firstName,
      lastName: info.lastName,
      role
    });
  }

  /** Logs out the current user. */
  logout(): void {
    this.userSignal.set(null);
  }

  /** Quick login helper for dev environment when user is logged out */
  loginAs(role: Role = Role.ADMIN): void {
    this.setMockRole(role);
  }
}
