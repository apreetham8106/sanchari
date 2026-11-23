import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { Ride } from '../../core/models/ride.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';
import { DriverRideService } from './services/driver-ride.service';

@Component({
  selector: 'app-earnings',
  standalone: true,
  imports: [DatePipe, StatusBadgeComponent, CurrencyInrPipe],
  template: `
    <div class="page">
      <h1>Earnings</h1>
      <div class="grid cols-3 mb">
        <div class="card stat"><span class="value">{{ todayTotal() | inr }}</span><span class="label">Today</span></div>
        <div class="card stat"><span class="value">{{ total() | inr }}</span><span class="label">Last 50 rides</span></div>
        <div class="card stat"><span class="value">{{ completedCount() }}</span><span class="label">Completed rides</span></div>
      </div>

      <div class="card">
        @if (rides().length === 0) {
          <p class="muted">No rides yet.</p>
        } @else {
          <div class="table-wrap">
            <table class="table">
              <thead><tr><th>When</th><th>Trip</th><th>Fare</th><th>Status</th></tr></thead>
              <tbody>
                @for (r of rides(); track r.id) {
                  <tr>
                    <td>{{ r.createdAt | date: 'dd MMM, h:mm a' }}</td>
                    <td>{{ r.pickupAddress || 'Pinned' }} → {{ r.dropAddress || 'Pinned' }}</td>
                    <td>{{ (r.finalFare ?? r.estimatedFare) | inr }}</td>
                    <td><app-status-badge [status]="r.status" /></td>
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
export class EarningsComponent implements OnInit {
  private readonly api = inject(DriverRideService);
  private readonly toast = inject(ToastService);

  readonly rides = signal<Ride[]>([]);

  private readonly completed = computed(() => this.rides().filter((r) => r.status === 'COMPLETED'));
  readonly completedCount = computed(() => this.completed().length);
  readonly total = computed(() => this.completed().reduce((sum, r) => sum + (r.finalFare ?? 0), 0));
  readonly todayTotal = computed(() => {
    const start = new Date();
    start.setHours(0, 0, 0, 0);
    return this.completed()
      .filter((r) => new Date(r.completedAt ?? r.createdAt) >= start)
      .reduce((sum, r) => sum + (r.finalFare ?? 0), 0);
  });

  ngOnInit(): void {
    this.api.history(0, 50).subscribe({
      next: (p) => this.rides.set(p.content),
      error: (e) => this.toast.error(errorMessage(e)),
    });
  }
}
