import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page-narrow">
      <h1>My profile</h1>
      <div class="card">
        @if (user(); as u) {
          <p class="muted small">{{ u.role }} · {{ u.phone }}</p>
          <label class="field">Full name<input name="fullName" [(ngModel)]="fullName" /></label>
          <label class="field">Email<input type="email" name="email" [(ngModel)]="email" /></label>
          <button class="btn primary" [disabled]="busy() || !fullName.trim()" (click)="save()">Save changes</button>
        } @else {
          <p class="muted">Loading…</p>
        }
      </div>
    </div>
  `,
})
export class ProfileComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);

  readonly user = signal<User | null>(null);
  readonly busy = signal(false);
  fullName = '';
  email = '';

  ngOnInit(): void {
    this.http.get<User>(`${environment.apiUrl}/users/me`).subscribe({
      next: (u) => this.apply(u),
      error: (e) => this.toast.error(errorMessage(e)),
    });
  }

  save(): void {
    this.busy.set(true);
    this.http.put<User>(`${environment.apiUrl}/users/me`, { fullName: this.fullName.trim(), email: this.email.trim() || null }).subscribe({
      next: (u) => {
        this.busy.set(false);
        this.apply(u);
        this.auth.setUser(u);
        this.toast.ok('Profile saved');
      },
      error: (e) => {
        this.busy.set(false);
        this.toast.error(errorMessage(e));
      },
    });
  }

  private apply(u: User): void {
    this.user.set(u);
    this.fullName = u.fullName;
    this.email = u.email ?? '';
  }
}
