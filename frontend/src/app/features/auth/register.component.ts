import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/http/error.util';
import { RegisterBody } from '../../core/models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="page-narrow">
      <h1 class="center">Create your Sanchari account</h1>
      <div class="card">
        <div class="tabs">
          <button type="button" [class.active]="role() === 'RIDER'" (click)="role.set('RIDER')">I need rides</button>
          <button type="button" [class.active]="role() === 'DRIVER'" (click)="role.set('DRIVER')">I drive</button>
        </div>

        @if (error()) {
          <div class="alert bad">{{ error() }}</div>
        }

        <label class="field">Full name<input name="fullName" [(ngModel)]="fullName" autocomplete="name" /></label>
        <label class="field">Phone number<input type="tel" name="phone" [(ngModel)]="phone" placeholder="98765 43210" autocomplete="tel" /></label>
        <label class="field">Email (optional)<input type="email" name="email" [(ngModel)]="email" autocomplete="email" /></label>
        <label class="field">
          Password (optional, at least 8 characters)
          <input type="password" name="password" [(ngModel)]="password" autocomplete="new-password" />
        </label>
        <p class="small muted">Without a password you can always sign in with an OTP.</p>

        <button class="btn primary block" [disabled]="busy() || !fullName || !phone" (click)="submit()">
          {{ busy() ? 'Creating…' : 'Create account' }}
        </button>
      </div>
      <p class="center mt small">Already registered? <a routerLink="/auth/login">Sign in</a></p>
    </div>
  `,
})
export class RegisterComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly role = signal<'RIDER' | 'DRIVER'>('RIDER');
  readonly busy = signal(false);
  readonly error = signal('');

  fullName = '';
  phone = '';
  email = '';
  password = '';

  submit(): void {
    if (this.busy()) {
      return;
    }
    if (this.password && this.password.length < 8) {
      this.error.set('Password must be at least 8 characters');
      return;
    }
    const body: RegisterBody = {
      fullName: this.fullName.trim(),
      phone: this.phone,
      role: this.role(),
      ...(this.email.trim() ? { email: this.email.trim() } : {}),
      ...(this.password ? { password: this.password } : {}),
    };
    this.busy.set(true);
    this.error.set('');
    this.auth.register(body).subscribe({
      next: () => void this.router.navigate(['/auth/verify'], { queryParams: { phone: this.phone, purpose: 'SIGNUP' } }),
      error: (e) => {
        this.busy.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }
}
