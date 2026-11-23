import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, finalize, shareReplay, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  MessageResponse,
  OtpPurpose,
  RegisterBody,
  Role,
  TokenPair,
  User,
} from '../models/user.model';
import { TokenStorage } from './token.storage';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly storage = inject(TokenStorage);
  private readonly base = `${environment.apiUrl}/auth`;

  private readonly _user = signal<User | null>(this.storage.user());
  readonly user = this._user.asReadonly();

  private refreshing$?: Observable<TokenPair>;

  isLoggedIn(): boolean {
    return !!this.storage.access() && !!this._user();
  }

  role(): Role | null {
    return this._user()?.role ?? null;
  }

  accessToken(): string | null {
    return this.storage.access();
  }

  hasRefreshToken(): boolean {
    return !!this.storage.refresh();
  }

  homeRoute(): string {
    switch (this.role()) {
      case 'RIDER':
        return '/rider';
      case 'DRIVER':
        return '/driver';
      case 'ADMIN':
        return '/admin';
      default:
        return '/auth/login';
    }
  }

  loginWithPassword(phone: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/login`, { phone, password }).pipe(tap((r) => this.persist(r)));
  }

  register(body: RegisterBody): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.base}/register`, body);
  }

  requestOtp(phone: string, purpose: OtpPurpose): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.base}/otp/request`, { phone, purpose });
  }

  verifyOtp(phone: string, code: string, purpose: OtpPurpose): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/otp/verify`, { phone, code, purpose }).pipe(tap((r) => this.persist(r)));
  }

  resetPassword(phone: string, code: string, newPassword: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.base}/password/reset`, { phone, code, newPassword });
  }

  /** One refresh at a time: concurrent 401s share the same in-flight request. */
  refreshTokens(): Observable<TokenPair> {
    if (!this.refreshing$) {
      const refreshToken = this.storage.refresh() ?? '';
      this.refreshing$ = this.http.post<TokenPair>(`${this.base}/refresh`, { refreshToken }).pipe(
        tap((t) => this.storage.setTokens(t.accessToken, t.refreshToken)),
        finalize(() => (this.refreshing$ = undefined)),
        shareReplay(1),
      );
    }
    return this.refreshing$;
  }

  setUser(user: User): void {
    this.storage.setUser(user);
    this._user.set(user);
  }

  logout(callServer = true): void {
    const refreshToken = this.storage.refresh();
    if (callServer && refreshToken) {
      this.http.post(`${this.base}/logout`, { refreshToken }).subscribe({ error: () => undefined });
    }
    this.storage.clear();
    this._user.set(null);
    void this.router.navigate(['/auth/login']);
  }

  private persist(r: AuthResponse): void {
    this.storage.setTokens(r.accessToken, r.refreshToken);
    this.setUser(r.user);
  }
}
