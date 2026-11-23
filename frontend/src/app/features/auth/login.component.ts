import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/http/error.util';
import { OtpInputComponent } from '../../shared/components/otp-input.component';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink, OtpInputComponent],
  template: `
    <div class="page-narrow">
      <h1 class="center brand">Sanchari</h1>
      <p class="muted center">Sign in to continue</p>

      <div class="card">
        <div class="tabs">
          <button type="button" [class.active]="mode() === 'password'" (click)="setMode('password')">Password</button>
          <button type="button" [class.active]="mode() === 'otp'" (click)="setMode('otp')">OTP</button>
        </div>

        @if (error()) {
          <div class="alert bad">{{ error() }}</div>
        }
        @if (devHints && mode() === 'otp') {
          <div class="alert info">Dev mode: the OTP is <b>123456</b>.</div>
        }

        <label class="field">
          Phone number
          <input type="tel" name="phone" [(ngModel)]="phone" placeholder="98765 43210" autocomplete="tel" />
        </label>

        @if (mode() === 'password') {
          <label class="field">
            Password
            <input type="password" name="password" [(ngModel)]="password" autocomplete="current-password" (keyup.enter)="loginPassword()" />
          </label>
          <button class="btn primary block" [disabled]="busy() || !phone || !password" (click)="loginPassword()">
            {{ busy() ? 'Signing in…' : 'Sign in' }}
          </button>
        } @else {
          @if (!otpSent()) {
            <button class="btn primary block" [disabled]="busy() || !phone" (click)="sendOtp()">
              {{ busy() ? 'Sending…' : 'Send OTP' }}
            </button>
          } @else {
            <label class="field">
              Enter the 6-digit code
              <app-otp-input [(value)]="code" (completed)="verifyOtp()" />
            </label>
            <button class="btn primary block" [disabled]="busy() || code.length < 6" (click)="verifyOtp()">
              {{ busy() ? 'Verifying…' : 'Verify and sign in' }}
            </button>
            <p class="small muted center mt">
              <a href="javascript:void(0)" (click)="sendOtp()">Resend code</a>
            </p>
          }
        }
      </div>

      <p class="center mt small">
        New to Sanchari? <a routerLink="/auth/register">Create an account</a><br />
        <a routerLink="/auth/reset">Forgot password?</a>
      </p>
    </div>
  `,
  styles: [`.brand { color: var(--ink); font-size: 2.2rem; letter-spacing: 0.02em; margin-top: 24px; }`],
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly devHints = environment.devHints;
  readonly mode = signal<'password' | 'otp'>('password');
  readonly busy = signal(false);
  readonly error = signal('');
  readonly otpSent = signal(false);

  phone = '';
  password = '';
  code = '';

  setMode(m: 'password' | 'otp'): void {
    this.mode.set(m);
    this.error.set('');
  }

  loginPassword(): void {
    if (this.busy() || !this.phone || !this.password) {
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.auth.loginWithPassword(this.phone, this.password).subscribe({
      next: () => void this.router.navigateByUrl(this.auth.homeRoute()),
      error: (e) => {
        this.busy.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }

  sendOtp(): void {
    if (this.busy() || !this.phone) {
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.auth.requestOtp(this.phone, 'LOGIN').subscribe({
      next: () => {
        this.busy.set(false);
        this.otpSent.set(true);
      },
      error: (e) => {
        this.busy.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }

  verifyOtp(): void {
    if (this.busy() || this.code.length < 6) {
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.auth.verifyOtp(this.phone, this.code, 'LOGIN').subscribe({
      next: () => void this.router.navigateByUrl(this.auth.homeRoute()),
      error: (e) => {
        this.busy.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }
}
