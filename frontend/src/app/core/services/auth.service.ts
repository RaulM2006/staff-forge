import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Role, User } from '../models/user.model';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest extends LoginRequest {
  firstName: string;
  lastName: string;
  role?: Role;
}

export interface AuthUserPayload {
  id: number | string;
  email: string;
  firstName: string;
  lastName: string;
  role?: Role | string;
  roles?: string[];
  enabled?: boolean;
}

export interface AuthResponse {
  token: string;
  type?: string;
  user: AuthUserPayload;
}

const TOKEN_STORAGE_KEY = 'staffforge_access_token';
const USER_STORAGE_KEY = 'staffforge_user';

const MOCK_USERS_BY_ROLE: Record<Role, { firstName: string; lastName: string; email: string }> = {
  [Role.ADMIN]: { firstName: 'Admin', lastName: 'User', email: 'admin@staffforge.dev' },
  [Role.EMPLOYEE]: { firstName: 'Ion', lastName: 'Popescu', email: 'employee@staffforge.dev' },
  [Role.PROJECT_MANAGER]: { firstName: 'Maria', lastName: 'Ionescu', email: 'pm@staffforge.dev' },
  [Role.RESOURCE_MANAGER]: { firstName: 'Andrei', lastName: 'Radu', email: 'rm@staffforge.dev' },
  [Role.HR]: { firstName: 'Elena', lastName: 'Dumitrescu', email: 'hr@staffforge.dev' }
};

function normalizeRole(role: Role | string | null | undefined): Role {
  if (!role) {
    return Role.EMPLOYEE;
  }

  const normalized = String(role).toUpperCase().replace(/^ROLE_/, '');

  if (normalized in Role) {
    return normalized as Role;
  }

  return Role.EMPLOYEE;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient, { optional: true });

  private readonly userSignal = signal<User | null>(this.readStoredUser() ?? {
    id: 1,
    ...MOCK_USERS_BY_ROLE[Role.ADMIN],
    role: Role.ADMIN
  });

  /** The currently authenticated user, or `null` if not logged in. */
  readonly currentUser = this.userSignal.asReadonly();

  /** Whether a user is currently authenticated. */
  readonly isAuthenticated = computed(() => this.currentUser() !== null);

  getToken(): string | null {
    if (typeof window === 'undefined') {
      return null;
    }

    return window.localStorage.getItem(TOKEN_STORAGE_KEY);
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    if (!this.http) {
      throw new Error('HttpClient is not available. Make sure provideHttpClient() is configured.');
    }

    return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
      tap((response) => this.persistAuthState(response)),
      catchError((error: HttpErrorResponse) => {
        this.logout();
        return throwError(() => error);
      })
    );
  }

  register(payload: RegisterRequest): Observable<AuthResponse> {
    if (!this.http) {
      throw new Error('HttpClient is not available. Make sure provideHttpClient() is configured.');
    }

    return this.http.post<AuthResponse>('/api/auth/register', payload).pipe(
      tap((response) => this.persistAuthState(response)),
      catchError((error: HttpErrorResponse) => {
        this.logout();
        return throwError(() => error);
      })
    );
  }

  /**
   * Switches the mock user's role and identity — useful for testing the role-based UI.
   */
  setMockRole(role: Role): void {
    const info = MOCK_USERS_BY_ROLE[role];
    const user: User = {
      id: 1,
      email: info.email,
      firstName: info.firstName,
      lastName: info.lastName,
      role
    };

    this.userSignal.set(user);
    this.persistUser(user);
  }

  /** Logs out the current user. */
  logout(): void {
    if (typeof window !== 'undefined') {
      window.localStorage.removeItem(TOKEN_STORAGE_KEY);
      window.localStorage.removeItem(USER_STORAGE_KEY);
    }

    this.userSignal.set(null);
  }

  /** Quick login helper for dev environment when user is logged out */
  loginAs(role: Role = Role.ADMIN): void {
    this.setMockRole(role);
  }

  private persistAuthState(response: AuthResponse): void {
    const user = this.mapUser(response.user);
    this.userSignal.set(user);

    if (typeof window !== 'undefined') {
      window.localStorage.setItem(TOKEN_STORAGE_KEY, response.token);
      window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
    }
  }

  private persistUser(user: User): void {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
    }
  }

  private readStoredUser(): User | null {
    if (typeof window === 'undefined') {
      return null;
    }

    const rawUser = window.localStorage.getItem(USER_STORAGE_KEY);
    if (!rawUser) {
      return null;
    }

    try {
      const parsed = JSON.parse(rawUser) as Partial<User>;
      if (!parsed.email || !parsed.role) {
        return null;
      }

      return {
        id: Number(parsed.id ?? 1),
        email: parsed.email,
        firstName: parsed.firstName ?? '',
        lastName: parsed.lastName ?? '',
        role: normalizeRole(parsed.role)
      };
    } catch {
      window.localStorage.removeItem(USER_STORAGE_KEY);
      return null;
    }
  }

  private mapUser(payload: AuthUserPayload): User {
    const role = normalizeRole(payload.role ?? payload.roles?.[0] ?? Role.EMPLOYEE);

    return {
      id: Number(payload.id ?? 1),
      email: payload.email,
      firstName: payload.firstName,
      lastName: payload.lastName,
      role
    };
  }
}
