import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { OtpInputComponent } from '../../shared/components/otp-input.component';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [FormsModule, RouterLink, OtpInputComponent],
  template: `
    <div class="page-narrow">
      <h1 class="center">Reset password</h1>
      <div class="card">
        @if (error()) {
          <div class="alert bad">{{ error() }}</div>
        }
        <label class="field">Phone number<input type="tel" name="phone" [(ngModel)]="phone" [disabled]="sent()" /></label>
        @if (!sent()) {
          <button class="btn primary block" [disabled]="busy() || !phone" (click)="send()">Send OTP</button>
        } @else {
          <label class="field">Code<app-otp-input [(value)]="code" /></label>
          <label class="field">New password (min 8 characters)<input type="password" name="pw" [(ngModel)]="newPassword" autocomplete="new-password" /></label>
          <button class="btn primary block" [disabled]="busy() || code.length < 6 || newPassword.length < 8" (click)="reset()">
            Set new password
          </button>
        }
      </div>
      <p class="center mt small"><a routerLink="/auth/login">Back to sign in</a></p>
    </div>
  `,
})
export class ResetPasswordComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly busy = signal(false);
  readonly sent = signal(false);
  readonly error = signal('');

  phone = '';
  code = '';
  newPassword = '';

  send(): void {
    this.busy.set(true);
    this.error.set('');
    this.auth.requestOtp(this.phone, 'RESET_PASSWORD').subscribe({
      next: () => {
        this.busy.set(false);
        this.sent.set(true);
      },
      error: (e) => {
        this.busy.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }

  reset(): void {
    this.busy.set(true);
    this.error.set('');
    this.auth.resetPassword(this.phone, this.code, this.newPassword).subscribe({
      next: () => {
        this.toast.ok('Password updated. Please sign in.');
        void this.router.navigate(['/auth/login']);
      },
      error: (e) => {
        this.busy.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }
}
