import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { Ride, RideStatus } from '../../core/models/ride.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';
import { AdminService } from './services/admin.service';

@Component({
  selector: 'app-admin-rides',
  standalone: true,
  imports: [FormsModule, DatePipe, StatusBadgeComponent, CurrencyInrPipe],
  template: `
    <div class="page">
      <div class="row between mb">
        <h1>Rides</h1>
        <select class="input" name="status" [(ngModel)]="status" (ngModelChange)="load()" style="width: auto">
          <option value="">All</option>
          <option value="SEARCHING">Searching</option>
          <option value="ACCEPTED">Accepted</option>
          <option value="ARRIVED">Arrived</option>
          <option value="STARTED">In progress</option>
          <option value="COMPLETED">Completed</option>
          <option value="CANCELLED">Cancelled</option>
          <option value="NO_DRIVER">No driver</option>
        </select>
      </div>
      <div class="card">
        <div class="table-wrap">
          <table class="table">
            <thead><tr><th>When</th><th>Ride</th><th>Vehicle</th><th>Driver</th><th>Radius</th><th>Fare</th><th>Status</th></tr></thead>
            <tbody>
              @for (r of rides(); track r.id) {
                <tr>
                  <td>{{ r.createdAt | date: 'dd MMM, h:mm a' }}</td>
                  <td><code>{{ r.id.slice(0, 8) }}</code></td>
                  <td>{{ r.vehicleType }}</td>
                  <td>{{ r.driver?.name || '—' }}</td>
                  <td>{{ r.searchRadiusM / 1000 }} km</td>
                  <td>{{ (r.finalFare ?? r.estimatedFare) | inr }}</td>
                  <td><app-status-badge [status]="r.status" /></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
        @if (rides().length === 0) {
          <p class="muted">No rides found.</p>
        }
      </div>
    </div>
  `,
})
export class AdminRidesComponent implements OnInit {
  private readonly api = inject(AdminService);
  private readonly toast = inject(ToastService);

  readonly rides = signal<Ride[]>([]);
  status: RideStatus | '' = '';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.rides(this.status, 0, 50).subscribe({
      next: (p) => this.rides.set(p.content),
      error: (e) => this.toast.error(errorMessage(e)),
    });
  }
}
