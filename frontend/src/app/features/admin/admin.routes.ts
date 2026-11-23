import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  { path: '', pathMatch: 'full', loadComponent: () => import('./admin-dashboard.component').then((m) => m.AdminDashboardComponent) },
  { path: 'drivers', loadComponent: () => import('./admin-drivers.component').then((m) => m.AdminDriversComponent) },
  { path: 'users', loadComponent: () => import('./admin-users.component').then((m) => m.AdminUsersComponent) },
  { path: 'rides', loadComponent: () => import('./admin-rides.component').then((m) => m.AdminRidesComponent) },
];
