import { Routes } from '@angular/router';
import { authGuard, guestGuard, homeRedirectGuard, roleGuard } from './core/auth/auth.guard';
import { ShellComponent } from './shared/layout/shell.component';

export const routes: Routes = [
  {
    path: 'auth',
    canActivate: [guestGuard],
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'rider',
        canActivate: [roleGuard],
        data: { roles: ['RIDER'] },
        loadChildren: () => import('./features/rider/rider.routes').then((m) => m.RIDER_ROUTES),
      },
      {
        path: 'driver',
        canActivate: [roleGuard],
        data: { roles: ['DRIVER'] },
        loadChildren: () => import('./features/driver/driver.routes').then((m) => m.DRIVER_ROUTES),
      },
      {
        path: 'admin',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile.component').then((m) => m.ProfileComponent),
      },
      { path: '', pathMatch: 'full', canActivate: [homeRedirectGuard], children: [] },
    ],
  },
  { path: '**', redirectTo: '' },
];
