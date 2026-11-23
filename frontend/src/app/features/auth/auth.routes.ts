import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
  { path: 'login', loadComponent: () => import('./login.component').then((m) => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./register.component').then((m) => m.RegisterComponent) },
  { path: 'verify', loadComponent: () => import('./otp-verify.component').then((m) => m.OtpVerifyComponent) },
  { path: 'reset', loadComponent: () => import('./reset-password.component').then((m) => m.ResetPasswordComponent) },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
];
