import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService, FgmRole } from '../services/auth.service';

/** Guard: user must be authenticated */
export const authGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated) return true;
  router.navigate(['/']);
  return false;
};

/** Guard: user must have at least one of the specified roles */
export function roleGuard(...roles: FgmRole[]): CanActivateFn {
  return () => {
    const auth   = inject(AuthService);
    const router = inject(Router);
    if (!auth.isAuthenticated) { router.navigate(['/']); return false; }
    if (auth.hasAnyRole(...roles)) return true;
    router.navigate(['/unauthorized']);
    return false;
  };
}

/** Guard: blocks INTERMEDIAIRE from accessing full dashboard */
export const dashboardGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated)      { router.navigate(['/']); return false; }
  if (auth.hasRole('INTERMEDIAIRE') && !auth.hasAnyRole('ADMIN_FGM', 'SUPERVISEUR')) {
    router.navigate(['/intermediaire']); return false;
  }
  return true;
};
