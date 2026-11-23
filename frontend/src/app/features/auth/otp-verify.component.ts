import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { OtpPurpose } from '../../core/models/user.model';
import { OtpInputComponent } from '../../shared/components/otp-input.component';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-otp-verify',
  standalone: true,
  imports: [FormsModule, RouterLink, OtpInputComponent],
  template: `
    <div class="page-narrow">
      <h1 class="center">Verify your phone</h1>
      <p class="muted center">We sent a 6-digit code to <b>{{ phone }}</b></p>
      <div class="card">
        @if (error()) {
          <div class="alert bad">{{ error() }}</div>
        }
        @if (devHints) {
          <div class="alert info">Dev mode: the OTP is <b>123456</b>.</div>
        }
        <label class="field">Code<app-otp-input [(value)]="code" (completed)="verify()" /></label>
        <button class="btn primary block" [disabled]="busy() || code.length < 6" (click)="verify()">
          {{ busy() ? 'Verifying…' : 'Verify' }}
        </button>
        <p class="small center mt"><a href="javascript:void(0)" (click)="resend()">Resend code</a></p>
      </div>
      <p class="center mt small"><a routerLink="/auth/login">Back to sign in</a></p>
    </div>
  `,
})
export class OtpVerifyComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);

  readonly devHints = environment.devHints;
  readonly busy = signal(false);
  readonly error = signal('');

  phone = this.route.snapshot.queryParamMap.get('phone') ?? '';
  purpose = (this.route.snapshot.queryParamMap.get('purpose') as OtpPurpose | null) ?? 'SIGNUP';
  code = '';

  verify(): void {
    if (this.busy() || this.code.length < 6) {
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.auth.verifyOtp(this.phone, this.code, this.purpose).subscribe({
      next: () => void this.router.navigateByUrl(this.auth.homeRoute()),
      error: (e) => {
        this.busy.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }

  resend(): void {
    this.auth.requestOtp(this.phone, this.purpose).subscribe({
      next: () => this.toast.ok('A new code was sent'),
      error: (e) => this.toast.error(errorMessage(e)),
    });
  }
}
