import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly hidePassword = signal(true);

  readonly loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  get email() {
    return this.loginForm.get('email');
  }

  get password() {
    return this.loginForm.get('password');
  }

  togglePasswordVisibility(): void {
    this.hidePassword.update((hidden) => !hidden);
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { email, password } = this.loginForm.getRawValue();

    this.authService.login({
      email: (email ?? '').trim(),
      password: password ?? ''
    }).subscribe({
      next: () => {
        this.isLoading.set(false);
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        const targetUrl = (returnUrl && returnUrl.startsWith('/') && !returnUrl.startsWith('//'))
          ? returnUrl
          : '/dashboard';

        this.router.navigateByUrl(targetUrl);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);

        if (error instanceof HttpErrorResponse) {
          if (error.status === 401) {
            this.errorMessage.set('Invalid email or password. Please verify your credentials and try again.');
          } else if (error.status === 0) {
            this.errorMessage.set('Unable to connect to the authentication server. Please verify your connection.');
          } else if (error.error?.message && typeof error.error.message === 'string') {
            this.errorMessage.set(error.error.message);
          } else {
            this.errorMessage.set('An unexpected error occurred during login. Please try again.');
          }
        } else {
          this.errorMessage.set('An unexpected error occurred. Please try again.');
        }
      }
    });
  }
}
