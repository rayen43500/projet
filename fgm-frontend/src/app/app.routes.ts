import { Routes } from '@angular/router';
import { authGuard, roleGuard, dashboardGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    // Welcome page is the home — login card is embedded in the hero section
    path: '',
    loadComponent: () => import('./components/auth/welcome-page.component').then(m => m.WelcomePageComponent),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard, dashboardGuard],
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent),
  },
  {
    path: 'intermediaire',
    canActivate: [authGuard, roleGuard('INTERMEDIAIRE', 'ADMIN_FGM')],
    loadComponent: () => import('./components/intermediaire/intermediaire.component').then(m => m.IntermediaireComponent),
  },
  {
    path: 'unauthorized',
    loadComponent: () => import('./components/auth/unauthorized.component').then(m => m.UnauthorizedComponent),
  },
  {
    path: '**',
    redirectTo: '',
  },
];
