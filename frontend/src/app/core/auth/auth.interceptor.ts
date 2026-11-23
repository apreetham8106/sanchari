import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

/** Adds the Bearer token, and on a 401 refreshes the token once and retries the request. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const isApi = req.url.startsWith(environment.apiUrl);
  const isAuthCall = req.url.startsWith(`${environment.apiUrl}/auth/`);
  const token = auth.accessToken();

  const authed = isApi && !isAuthCall && token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(authed).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status !== 401 || !isApi || isAuthCall) {
        return throwError(() => err);
      }
      if (!auth.hasRefreshToken()) {
        auth.logout(false);
        return throwError(() => err);
      }
      return auth.refreshTokens().pipe(
        switchMap((t) => next(req.clone({ setHeaders: { Authorization: `Bearer ${t.accessToken}` } }))),
        catchError((e) => {
          auth.logout(false);
          return throwError(() => e);
        }),
      );
    }),
  );
};
