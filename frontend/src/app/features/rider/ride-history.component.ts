import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { Ride } from '../../core/models/ride.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';
import { RiderRideService } from './services/rider-ride.service';

@Component({
  selector: 'app-ride-history',
  standalone: true,
  imports: [DatePipe, RouterLink, StatusBadgeComponent, CurrencyInrPipe],
  template: `
    <div class="page">
      <h1>My rides</h1>
      <div class="card">
        @if (loading()) {
          <p class="muted">Loading…</p>
        } @else if (rides().length === 0) {
          <p class="muted">No rides yet. <a routerLink="/rider">Book your first ride</a>.</p>
        } @else {
          <div class="table-wrap">
            <table class="table">
              <thead><tr><th>When</th><th>Trip</th><th>Vehicle</th><th>Fare</th><th>Status</th><th></th></tr></thead>
              <tbody>
                @for (r of rides(); track r.id) {
                  <tr>
                    <td>{{ r.createdAt | date: 'dd MMM, h:mm a' }}</td>
                    <td>{{ r.pickupAddress || 'Pinned' }} → {{ r.dropAddress || 'Pinned' }}</td>
                    <td>{{ r.vehicleType }}</td>
                    <td>{{ (r.finalFare ?? r.estimatedFare) | inr }}</td>
                    <td><app-status-badge [status]="r.status" /></td>
                    <td><a [routerLink]="['/rider/ride', r.id]">Open</a></td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>
    </div>
  `,
})
export class RideHistoryComponent implements OnInit {
  private readonly api = inject(RiderRideService);
  private readonly toast = inject(ToastService);

  readonly rides = signal<Ride[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.api.history(0, 50).subscribe({
      next: (p) => {
        this.rides.set(p.content);
        this.loading.set(false);
      },
      error: (e) => {
        this.loading.set(false);
        this.toast.error(errorMessage(e));
      },
    });
  }
}
