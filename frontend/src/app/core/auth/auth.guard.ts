import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models/user.model';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isLoggedIn() ? true : router.parseUrl('/auth/login');
};

/** Route data: { roles: ['DRIVER'] }. Wrong role goes to that user's own home. */
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const allowed = (route.data['roles'] as Role[] | undefined) ?? [];
  const role = auth.role();
  return role && allowed.includes(role) ? true : router.parseUrl(auth.homeRoute());
};

/** For "/": send the user to their role's home. */
export const homeRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return inject(Router).parseUrl(auth.homeRoute());
};

/** Login/register pages: skip them when already signed in. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isLoggedIn() ? inject(Router).parseUrl(auth.homeRoute()) : true;
};
