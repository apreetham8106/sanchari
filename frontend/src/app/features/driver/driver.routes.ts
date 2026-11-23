import { Routes } from '@angular/router';

export const DRIVER_ROUTES: Routes = [
  { path: '', pathMatch: 'full', loadComponent: () => import('./driver-dashboard.component').then((m) => m.DriverDashboardComponent) },
  { path: 'onboarding', loadComponent: () => import('./driver-onboarding.component').then((m) => m.DriverOnboardingComponent) },
  { path: 'ride/:id', loadComponent: () => import('./active-ride.component').then((m) => m.ActiveRideComponent) },
  { path: 'earnings', loadComponent: () => import('./earnings.component').then((m) => m.EarningsComponent) },
];
