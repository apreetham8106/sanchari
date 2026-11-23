import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { DashboardStats, KycEvent } from '../../core/models/driver.model';
import { WebSocketService } from '../../core/realtime/websocket.service';
import { AdminService } from './services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="page">
      <h1>Dashboard</h1>
      @if (stats(); as s) {
        <div class="grid cols-4 mb">
          <div class="card stat"><span class="value">{{ s.activeRides }}</span><span class="label">Active rides</span></div>
          <div class="card stat"><span class="value">{{ s.onlineDrivers }}</span><span class="label">Drivers online</span></div>
          <div class="card stat"><span class="value">{{ s.ridesToday }}</span><span class="label">Rides today</span></div>
          <div class="card stat">
            <span class="value">{{ s.pendingKyc }}</span>
            <span class="label">KYC pending · <a routerLink="/admin/drivers">review</a></span>
          </div>
          <div class="card stat"><span class="value">{{ s.totalRiders }}</span><span class="label">Riders</span></div>
          <div class="card stat"><span class="value">{{ s.totalDrivers }}</span><span class="label">Drivers</span></div>
          <div class="card stat"><span class="value">{{ s.completedRides }}</span><span class="label">Completed rides</span></div>
          <div class="card stat"><span class="value">{{ s.cancelledRides }}</span><span class="label">Cancelled rides</span></div>
        </div>
      } @else {
        <p class="muted">Loading…</p>
      }

      <div class="card">
        <h3>Live verification feed</h3>
        @if (feed().length === 0) {
          <p class="muted small">Verification updates appear here as they happen.</p>
        }
        @for (e of feed(); track $index) {
          <div class="small">
            {{ e.subject === 'DRIVER' ? 'Driver identity' : 'Vehicle' }} for <code>{{ e.driverId.slice(0, 8) }}</code>:
            <b>{{ e.status.replace('_', ' ') }}</b>
          </div>
        }
      </div>
    </div>
  `,
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  private readonly api = inject(AdminService);
  private readonly ws = inject(WebSocketService);
  private readonly toast = inject(ToastService);

  readonly stats = signal<DashboardStats | null>(null);
  readonly feed = signal<KycEvent[]>([]);
  private subs = new Subscription();

  ngOnInit(): void {
    this.load();
    this.subs.add(interval(15000).subscribe(() => this.load()));
    this.ws.connect();
    this.subs.add(
      this.ws.watch<KycEvent>('/topic/admin/kyc').subscribe((e) => {
        this.feed.update((list) => [e, ...list].slice(0, 8));
        this.load();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  private load(): void {
    this.api.dashboard().subscribe({
      next: (s) => this.stats.set(s),
      error: (e) => this.toast.error(errorMessage(e)),
    });
  }
}
