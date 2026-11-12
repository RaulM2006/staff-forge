import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { authGuard } from './auth.guard';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/user.model';

describe('Security Guards (authGuard & roleGuard)', () => {
  let authService: AuthService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  it('authGuard should allow access when user is authenticated (default ADMIN)', () => {
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url: '/dashboard' } as RouterStateSnapshot)
    );
    expect(result).toBeTrue();
  });

  it('authGuard should redirect to /login when user is logged out', () => {
    authService.logout();
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url: '/projects' } as RouterStateSnapshot)
    ) as UrlTree;

    expect(router.serializeUrl(result)).toBe('/login?returnUrl=%2Fprojects');
  });

  it('roleGuard should block EMPLOYEE from accessing /allocations and redirect to /dashboard', () => {
    authService.setMockRole(Role.EMPLOYEE);
    const route = { data: { roles: [Role.ADMIN] } } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, { url: '/allocations' } as RouterStateSnapshot)
    ) as UrlTree;

    expect(router.serializeUrl(result)).toBe('/dashboard');
  });

  it('roleGuard should allow PROJECT_MANAGER to access /projects', () => {
    authService.setMockRole(Role.PROJECT_MANAGER);
    const route = {
      data: { roles: [Role.ADMIN, Role.PROJECT_MANAGER, Role.RESOURCE_MANAGER] }
    } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, { url: '/projects' } as RouterStateSnapshot)
    );

    expect(result).toBeTrue();
  });

  it('roleGuard should allow ADMIN to access /admin/skills', () => {
    authService.setMockRole(Role.ADMIN);
    const route = { data: { roles: [Role.ADMIN] } } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, { url: '/admin/skills' } as RouterStateSnapshot)
    );

    expect(result).toBeTrue();
  });

  it('roleGuard should block non-admin from accessing /admin/skills and redirect to /dashboard', () => {
    authService.setMockRole(Role.EMPLOYEE);
    const route = { data: { roles: [Role.ADMIN] } } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, { url: '/admin/skills' } as RouterStateSnapshot)
    ) as UrlTree;

    expect(router.serializeUrl(result)).toBe('/dashboard');
  });
});
