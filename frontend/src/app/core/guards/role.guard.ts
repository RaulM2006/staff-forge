import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/user.model';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.currentUser();

  if (!user) {
    return router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url }
    });
  }

  const allowedRoles = route.data?.['roles'] as Role[] | undefined;

  if (!allowedRoles || allowedRoles.length === 0 || allowedRoles.includes(user.role)) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
