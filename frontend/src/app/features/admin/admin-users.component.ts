import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { Role, User } from '../../core/models/user.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { AdminService } from './services/admin.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [FormsModule, DatePipe, StatusBadgeComponent],
  template: `
    <div class="page">
      <div class="row between mb">
        <h1>Users</h1>
        <div class="row">
          <select class="input" name="role" [(ngModel)]="role" (ngModelChange)="load()">
            <option value="">All roles</option>
            <option value="RIDER">Riders</option>
            <option value="DRIVER">Drivers</option>
            <option value="ADMIN">Admins</option>
          </select>
          <input class="input" name="q" [(ngModel)]="q" placeholder="Search name or phone" (keyup.enter)="load()" />
          <button class="btn" (click)="load()">Search</button>
        </div>
      </div>

      <div class="card">
        <div class="table-wrap">
          <table class="table">
            <thead><tr><th>Name</th><th>Phone</th><th>Role</th><th>Status</th><th>Joined</th><th></th></tr></thead>
            <tbody>
              @for (u of users(); track u.id) {
                <tr>
                  <td>{{ u.fullName }}</td>
                  <td>{{ u.phone }}</td>
                  <td>{{ u.role }}</td>
                  <td><app-status-badge [status]="u.status" /></td>
                  <td>{{ u.createdAt | date: 'dd MMM yyyy' }}</td>
                  <td class="right">
                    @if (u.role !== 'ADMIN') {
                      @if (u.status === 'BLOCKED') {
                        <button class="btn sm" (click)="setBlocked(u, false)">Unblock</button>
                      } @else {
                        <button class="btn sm danger" (click)="setBlocked(u, true)">Block</button>
                      }
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
        @if (users().length === 0) {
          <p class="muted">No users found.</p>
        }
      </div>
    </div>
  `,
})
export class AdminUsersComponent implements OnInit {
  private readonly api = inject(AdminService);
  private readonly toast = inject(ToastService);

  readonly users = signal<User[]>([]);
  role: Role | '' = '';
  q = '';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.users(this.role, this.q, 0, 50).subscribe({
      next: (p) => this.users.set(p.content),
      error: (e) => this.toast.error(errorMessage(e)),
    });
  }

  setBlocked(u: User, blocked: boolean): void {
    if (blocked && !confirm(`Block ${u.fullName}? They will be signed out.`)) {
      return;
    }
    this.api.setBlocked(u.id, blocked).subscribe({
      next: () => {
        this.toast.ok(blocked ? 'User blocked' : 'User unblocked');
        this.load();
      },
      error: (e) => this.toast.error(errorMessage(e)),
    });
  }
}
