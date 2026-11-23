import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { AdminDriver, KycStatus } from '../../core/models/driver.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { AdminService } from './services/admin.service';

@Component({
  selector: 'app-admin-drivers',
  standalone: true,
  imports: [FormsModule, StatusBadgeComponent],
  template: `
    <div class="page">
      <div class="row between mb">
        <h1>Drivers</h1>
        <label>
          <select class="input" name="kyc" [ngModel]="filter()" (ngModelChange)="setFilter($event)">
            <option value="">All</option>
            <option value="IN_PROGRESS">KYC in progress</option>
            <option value="VERIFIED">Verified</option>
            <option value="REJECTED">Rejected</option>
            <option value="NOT_STARTED">Not started</option>
          </select>
        </label>
      </div>

      <div class="card">
        @if (drivers().length === 0) {
          <p class="muted">No drivers found.</p>
        }
        <div class="table-wrap">
          <table class="table">
            <tbody>
              @for (d of drivers(); track d.userId) {
                <tr>
                  <td style="width: 28%">
                    <b>{{ d.fullName }}</b><br /><span class="muted small">{{ d.phone }}</span>
                    @if (d.online) {
                      <span class="badge ok">ONLINE</span>
                    }
                  </td>
                  <td style="width: 32%">
                    <app-status-badge [status]="d.kycStatus" />
                    <div class="small muted">Licence {{ d.licenseNumber || '—' }}</div>
                    <div class="row mt">
                      <button class="btn sm ok" [disabled]="d.kycStatus === 'VERIFIED'" (click)="reviewDriver(d, true)">Approve</button>
                      <button class="btn sm danger" [disabled]="d.kycStatus === 'REJECTED'" (click)="reviewDriver(d, false)">Reject</button>
                    </div>
                  </td>
                  <td>
                    @for (v of d.vehicles; track v.id) {
                      <div class="row between" style="margin-bottom: 6px">
                        <span><b>{{ v.registrationNo }}</b> <span class="muted small">{{ v.vehicleType }}</span></span>
                        <span class="row">
                          <app-status-badge [status]="v.rcStatus" />
                          <button class="btn sm ok" [disabled]="v.rcStatus === 'VERIFIED'" (click)="reviewVehicle(v.id, true)">✓</button>
                          <button class="btn sm danger" [disabled]="v.rcStatus === 'REJECTED'" (click)="reviewVehicle(v.id, false)">✗</button>
                        </span>
                      </div>
                    }
                    @if (d.vehicles.length === 0) {
                      <span class="muted small">No vehicles</span>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
})
export class AdminDriversComponent implements OnInit {
  private readonly api = inject(AdminService);
  private readonly toast = inject(ToastService);

  readonly drivers = signal<AdminDriver[]>([]);
  readonly filter = signal<KycStatus | ''>('IN_PROGRESS');

  ngOnInit(): void {
    this.load();
  }

  setFilter(v: KycStatus | ''): void {
    this.filter.set(v);
    this.load();
  }

  reviewDriver(d: AdminDriver, approve: boolean): void {
    this.api.reviewDriver(d.userId, approve).subscribe({
      next: (m) => {
        this.toast.ok(m.message);
        this.load();
      },
      error: (e) => this.toast.error(errorMessage(e)),
    });
  }

  reviewVehicle(id: string, approve: boolean): void {
    this.api.reviewVehicle(id, approve).subscribe({
      next: (m) => {
        this.toast.ok(m.message);
        this.load();
      },
      error: (e) => this.toast.error(errorMessage(e)),
    });
  }

  private load(): void {
    this.api.drivers(this.filter(), 0, 50).subscribe({
      next: (p) => this.drivers.set(p.content),
      error: (e) => this.toast.error(errorMessage(e)),
    });
  }
}
