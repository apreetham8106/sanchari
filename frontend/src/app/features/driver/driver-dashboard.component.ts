import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { GeolocationService } from '../../core/geo/geolocation.service';
import { DriverProfile } from '../../core/models/driver.model';
import { RideOffer } from '../../core/models/ride.model';
import { WebSocketService } from '../../core/realtime/websocket.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';
import { DriverRideService } from './services/driver-ride.service';
import { DriverService } from './services/driver.service';
import { LocationReporterService } from './services/location-reporter.service';

@Component({
  selector: 'app-driver-dashboard',
  standalone: true,
  imports: [RouterLink, StatusBadgeComponent, CurrencyInrPipe],
  template: `
    <div class="page">
      @if (profile(); as p) {
        <div class="card mb">
          <div class="row between">
            <div>
              <h2>{{ p.online ? "You're online" : "You're offline" }}</h2>
              <div class="muted small">
                KYC <app-status-badge [status]="p.kycStatus" /> · Rating ★ {{ p.rating }}
              </div>
            </div>
            <button class="btn" [class.ok]="!p.online" [class.danger]="p.online" [disabled]="busy() || (!p.online && !canGoOnline())" (click)="toggleOnline()">
              {{ p.online ? 'Go offline' : 'Go online' }}
            </button>
          </div>
          @if (!canGoOnline()) {
            <div class="alert warn mt">
              Finish onboarding to start taking rides: a verified licence and at least one verified vehicle are required.
              <a routerLink="/driver/onboarding">Open onboarding</a>
            </div>
          }
        </div>

        @if (p.online) {
          <h3>Ride requests</h3>
          @if (visibleOffers().length === 0) {
            <div class="card muted pulse">Waiting for ride requests near you…</div>
          }
          <div class="grid cols-2">
            @for (o of visibleOffers(); track o.rideId) {
              <div class="card offer">
                <div class="row between">
                  <b>{{ o.estimatedFare | inr }}</b>
                  <span class="badge warn">{{ secondsLeft(o) }}s</span>
                </div>
                <p class="mb small">
                  <b>Pickup</b> {{ o.pickupAddress || 'Pinned location' }} · {{ distanceText(o.distanceToPickupM) }} away<br />
                  <b>Drop</b> {{ o.dropAddress || 'Pinned location' }}
                </p>
                <div class="row">
                  <button class="btn primary" style="flex: 1" [disabled]="busy()" (click)="accept(o)">Accept</button>
                  <button class="btn" [disabled]="busy()" (click)="reject(o)">Decline</button>
                </div>
              </div>
            }
          </div>
        }
      } @else {
        <p class="muted">Loading…</p>
      }
    </div>
  `,
})
export class DriverDashboardComponent implements OnInit, OnDestroy {
  private readonly driverApi = inject(DriverService);
  private readonly rideApi = inject(DriverRideService);
  private readonly reporter = inject(LocationReporterService);
  private readonly geo = inject(GeolocationService);
  private readonly ws = inject(WebSocketService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly profile = signal<DriverProfile | null>(null);
  readonly offers = signal<RideOffer[]>([]);
  readonly now = signal(Date.now());
  readonly busy = signal(false);

  private subs = new Subscription();

  readonly canGoOnline = computed(() => {
    const p = this.profile();
    return !!p && p.kycStatus === 'VERIFIED' && p.vehicles.some((v) => v.rcStatus === 'VERIFIED');
  });

  readonly visibleOffers = computed(() => this.offers().filter((o) => new Date(o.expiresAt).getTime() > this.now()));

  ngOnInit(): void {
    this.rideApi.active().subscribe({
      next: (r) => {
        if (r) {
          void this.router.navigate(['/driver/ride', r.id]);
        }
      },
      error: () => undefined,
    });
    this.driverApi.profile().subscribe({
      next: (p) => {
        this.profile.set(p);
        if (p.online) {
          this.reporter.start();
          this.loadOffers();
        }
      },
      error: (e) => this.toast.error(errorMessage(e)),
    });

    this.ws.connect();
    this.subs.add(
      this.ws.watch<RideOffer>('/user/queue/offers').subscribe((o) => {
        this.offers.update((list) => (list.some((x) => x.rideId === o.rideId) ? list : [o, ...list]));
        this.toast.show('New ride request');
      }),
    );
    this.subs.add(
      this.ws.watch<{ rideId: string }>('/user/queue/offer-withdrawn').subscribe((m) => this.removeOffer(m.rideId)),
    );
    this.subs.add(interval(1000).subscribe(() => this.now.set(Date.now())));
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  toggleOnline(): void {
    const p = this.profile();
    if (!p || this.busy()) {
      return;
    }
    this.busy.set(true);
    if (p.online) {
      this.driverApi.setOnline(false).subscribe({
        next: (np) => {
          this.reporter.stop();
          this.offers.set([]);
          this.profile.set(np);
          this.busy.set(false);
        },
        error: (e) => {
          this.busy.set(false);
          this.toast.error(errorMessage(e));
        },
      });
      return;
    }
    this.geo
      .getCurrent()
      .then((pos) => {
        this.driverApi.setOnline(true, pos.lat, pos.lng).subscribe({
          next: (np) => {
            this.profile.set(np);
            this.reporter.start(pos);
            this.loadOffers();
            this.busy.set(false);
          },
          error: (e) => {
            this.busy.set(false);
            this.toast.error(errorMessage(e));
          },
        });
      })
      .catch((e: Error) => {
        this.busy.set(false);
        this.toast.error(e.message);
      });
  }

  accept(o: RideOffer): void {
    this.busy.set(true);
    this.rideApi.accept(o.rideId).subscribe({
      next: (ride) => void this.router.navigate(['/driver/ride', ride.id]),
      error: (e) => {
        this.busy.set(false);
        this.removeOffer(o.rideId);
        this.toast.error(errorMessage(e));
      },
    });
  }

  reject(o: RideOffer): void {
    this.removeOffer(o.rideId);
    this.rideApi.reject(o.rideId).subscribe({ error: () => undefined });
  }

  secondsLeft(o: RideOffer): number {
    return Math.max(0, Math.round((new Date(o.expiresAt).getTime() - this.now()) / 1000));
  }

  distanceText(m: number): string {
    return m >= 1000 ? `${(m / 1000).toFixed(1)} km` : `${Math.round(m)} m`;
  }

  private loadOffers(): void {
    this.rideApi.offers().subscribe({
      next: (list) => this.offers.update((cur) => [...list.filter((o) => !cur.some((c) => c.rideId === o.rideId)), ...cur]),
      error: () => undefined,
    });
  }

  private removeOffer(rideId: string): void {
    this.offers.update((list) => list.filter((o) => o.rideId !== rideId));
  }
}
