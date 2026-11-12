import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { Role } from '../../../core/models/user.model';
import { By } from '@angular/platform-browser';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let mockActivatedRoute: { snapshot: { queryParamMap: { get: jasmine.Spy } } };

  const mockAuthResponse: AuthResponse = {
    token: 'jwt-mock-token-xyz',
    type: 'Bearer',
    user: {
      id: 1,
      email: 'user@staffforge.dev',
      firstName: 'Jane',
      lastName: 'Doe',
      role: Role.ADMIN
    }
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);
    routerSpy = jasmine.createSpyObj('Router', ['navigateByUrl']);
    mockActivatedRoute = {
      snapshot: {
        queryParamMap: {
          get: jasmine.createSpy('get').and.returnValue(null)
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component with initial state', () => {
    expect(component).toBeTruthy();
    expect(component.loginForm).toBeDefined();
    expect(component.loginForm.valid).toBeFalse();
    expect(component.isLoading()).toBeFalse();
    expect(component.errorMessage()).toBeNull();
    expect(component.hidePassword()).toBeTrue();
  });

  describe('Form Validation', () => {
    it('should validate email field as required', () => {
      const emailControl = component.email;
      expect(emailControl?.valid).toBeFalse();
      expect(emailControl?.errors?.['required']).toBeTrue();
    });

    it('should validate email format', () => {
      const emailControl = component.email;
      emailControl?.setValue('not-an-email');
      expect(emailControl?.valid).toBeFalse();
      expect(emailControl?.errors?.['email']).toBeTrue();

      emailControl?.setValue('valid.user@staffforge.dev');
      expect(emailControl?.valid).toBeTrue();
    });

    it('should validate password field as required', () => {
      const passwordControl = component.password;
      expect(passwordControl?.valid).toBeFalse();
      expect(passwordControl?.errors?.['required']).toBeTrue();
    });

    it('should validate minimum password length of 6 characters', () => {
      const passwordControl = component.password;
      passwordControl?.setValue('12345');
      expect(passwordControl?.valid).toBeFalse();
      expect(passwordControl?.errors?.['minlength']).toBeTruthy();
      expect(passwordControl?.errors?.['minlength'].requiredLength).toBe(6);

      passwordControl?.setValue('123456');
      expect(passwordControl?.valid).toBeTrue();
    });

    it('should enable valid status when both fields meet criteria', () => {
      component.loginForm.setValue({
        email: 'admin@staffforge.dev',
        password: 'Password123!'
      });
      expect(component.loginForm.valid).toBeTrue();
    });
  });

  describe('Form Submission', () => {
    it('should not submit if form is invalid and should mark controls as touched', () => {
      spyOn(component.loginForm, 'markAllAsTouched');
      component.onSubmit();

      expect(component.loginForm.markAllAsTouched).toHaveBeenCalled();
      expect(authServiceSpy.login).not.toHaveBeenCalled();
    });

    it('should show validation error messages in the DOM when controls are touched and invalid', () => {
      component.onSubmit();
      fixture.detectChanges();

      const emailError = fixture.debugElement.query(By.css('#email-error'));
      const passwordError = fixture.debugElement.query(By.css('#password-error'));

      expect(emailError).toBeTruthy();
      expect(emailError.nativeElement.textContent).toContain('Email address is required');
      expect(passwordError).toBeTruthy();
      expect(passwordError.nativeElement.textContent).toContain('Password is required');
    });

    it('should submit valid credentials and redirect to /dashboard by default', () => {
      authServiceSpy.login.and.returnValue(of(mockAuthResponse));

      component.loginForm.setValue({
        email: 'admin@staffforge.dev',
        password: 'SecurePassword123'
      });

      component.onSubmit();

      expect(authServiceSpy.login).toHaveBeenCalledWith({
        email: 'admin@staffforge.dev',
        password: 'SecurePassword123'
      });
      expect(component.isLoading()).toBeFalse();
      expect(routerSpy.navigateByUrl).toHaveBeenCalledWith('/dashboard');
    });

    it('should redirect to returnUrl when specified in route query params', () => {
      mockActivatedRoute.snapshot.queryParamMap.get.and.returnValue('/projects');
      authServiceSpy.login.and.returnValue(of(mockAuthResponse));

      component.loginForm.setValue({
        email: 'pm@staffforge.dev',
        password: 'SecurePassword123'
      });

      component.onSubmit();

      expect(routerSpy.navigateByUrl).toHaveBeenCalledWith('/projects');
    });

    it('should fallback to /dashboard if returnUrl is an external/protocol-relative URL', () => {
      mockActivatedRoute.snapshot.queryParamMap.get.and.returnValue('//evil.example.com');
      authServiceSpy.login.and.returnValue(of(mockAuthResponse));

      component.loginForm.setValue({
        email: 'pm@staffforge.dev',
        password: 'SecurePassword123'
      });

      component.onSubmit();

      expect(routerSpy.navigateByUrl).toHaveBeenCalledWith('/dashboard');
    });

    it('should display error message on HTTP 401 unauthorized response', () => {
      const errorResponse = new HttpErrorResponse({
        status: 401,
        statusText: 'Unauthorized',
        error: { message: 'Bad credentials' }
      });
      authServiceSpy.login.and.returnValue(throwError(() => errorResponse));

      component.loginForm.setValue({
        email: 'wrong@staffforge.dev',
        password: 'WrongPassword'
      });

      component.onSubmit();
      fixture.detectChanges();

      expect(component.isLoading()).toBeFalse();
      expect(component.errorMessage()).toContain('Invalid email or password');

      const alertElement = fixture.debugElement.query(By.css('[role="alert"]'));
      expect(alertElement).toBeTruthy();
      expect(alertElement.nativeElement.textContent).toContain('Invalid email or password');
    });

    it('should display custom server error message when provided in error response', () => {
      const errorResponse = new HttpErrorResponse({
        status: 400,
        statusText: 'Bad Request',
        error: { message: 'Account is locked due to multiple failed attempts' }
      });
      authServiceSpy.login.and.returnValue(throwError(() => errorResponse));

      component.loginForm.setValue({
        email: 'locked@staffforge.dev',
        password: 'ValidPassword123'
      });

      component.onSubmit();
      fixture.detectChanges();

      expect(component.isLoading()).toBeFalse();
      expect(component.errorMessage()).toBe('Account is locked due to multiple failed attempts');
    });
  });

  describe('Password Toggle', () => {
    it('should toggle password visibility when button clicked', () => {
      expect(component.hidePassword()).toBeTrue();

      component.togglePasswordVisibility();
      expect(component.hidePassword()).toBeFalse();

      component.togglePasswordVisibility();
      expect(component.hidePassword()).toBeTrue();
    });

    it('should change password input type in template when toggled', () => {
      const passwordInput = fixture.debugElement.query(By.css('#password')).nativeElement;
      expect(passwordInput.type).toBe('password');

      const toggleButton = fixture.debugElement.query(By.css('button[type="button"]')).nativeElement;
      toggleButton.click();
      fixture.detectChanges();

      expect(passwordInput.type).toBe('text');
    });
  });
});
