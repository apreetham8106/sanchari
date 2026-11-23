import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { Ride, RideStatusEvent } from '../../core/models/ride.model';
import { WebSocketService } from '../../core/realtime/websocket.service';
import { MapMarker, MapViewComponent } from '../../shared/components/map-view.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';
import { DriverRideService } from './services/driver-ride.service';
import { LocationReporterService } from './services/location-reporter.service';

@Component({
  selector: 'app-active-ride',
  standalone: true,
  imports: [MapViewComponent, StatusBadgeComponent, CurrencyInrPipe, RouterLink],
  template: `
    <div class="page">
      @if (ride(); as r) {
        <div class="split">
          <div class="card">
            <div class="row between">
              <h2>{{ headline() }}</h2>
              <app-status-badge [status]="r.status" />
            </div>

            <div class="small mb">
              <div><b>Pickup</b> {{ r.pickupAddress || 'Pinned location' }}</div>
              <div><b>Drop</b> {{ r.dropAddress || 'Pinned location' }}</div>
            </div>
            <div class="row between mb">
              <span class="muted">Fare</span>
              <b>{{ (r.finalFare ?? r.estimatedFare) | inr }}</b>
            </div>

            @switch (r.status) {
              @case ('ACCEPTED') {
                <button class="btn primary block" [disabled]="busy()" (click)="arrived()">I have arrived at pickup</button>
              }
              @case ('ARRIVED') {
                <button class="btn primary block" [disabled]="busy()" (click)="start()">Start ride</button>
              }
              @case ('STARTED') {
                <button class="btn ok block" [disabled]="busy()" (click)="complete()">Complete ride</button>
              }
              @case ('COMPLETED') {
                <div class="alert ok">Ride complete. You earned {{ r.finalFare | inr }}.</div>
                <a class="btn primary block" routerLink="/driver">Back to dashboard</a>
              }
              @case ('CANCELLED') {
                <div class="alert warn">The rider cancelled this ride.</div>
                <a class="btn primary block" routerLink="/driver">Back to dashboard</a>
              }
            }
          </div>

          <div class="map-box tall">
            <app-map-view [center]="center()" [markers]="markers()" [fit]="true" />
          </div>
        </div>
      } @else {
        <p class="muted">Loading ride…</p>
      }
    </div>
  `,
})
export class ActiveRideComponent implements OnInit, OnDestroy {
  private readonly api = inject(DriverRideService);
  private readonly reporter = inject(LocationReporterService);
  private readonly ws = inject(WebSocketService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly ride = signal<Ride | null>(null);
  readonly busy = signal(false);

  private id = '';
  private sub?: Subscription;

  readonly headline = computed(() => {
    switch (this.ride()?.status) {
      case 'ACCEPTED':
        return 'Head to the pickup';
      case 'ARRIVED':
        return 'Waiting for the rider';
      case 'STARTED':
        return 'Ride in progress';
      case 'COMPLETED':
        return 'Ride complete';
      default:
        return 'Ride cancelled';
    }
  });

  readonly center = computed(() => {
    const r = this.ride();
    return r ? { lat: r.pickupLat, lng: r.pickupLng } : { lat: 17.385, lng: 78.4867 };
  });

  readonly markers = computed<MapMarker[]>(() => {
    const r = this.ride();
    if (!r) {
      return [];
    }
    return [
      { id: 'pickup', lat: r.pickupLat, lng: r.pickupLng, label: 'Pickup', color: '#138a55' },
      { id: 'drop', lat: r.dropLat, lng: r.dropLng, label: 'Drop', color: '#c0283f' },
    ];
  });

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id') ?? '';
    this.reporter.start(); // keep the rider's map live while on a ride
    this.api.active().subscribe({
      next: (r) => {
        if (!r || r.id !== this.id) {
          void this.router.navigate(['/driver']);
          return;
        }
        this.ride.set(r);
      },
      error: (e) => this.toast.error(errorMessage(e)),
    });
    this.ws.connect();
    this.sub = this.ws.watch<RideStatusEvent>(`/topic/rides/${this.id}`).subscribe((e) => {
      if (e.status === 'CANCELLED') {
        this.ride.update((r) => (r ? { ...r, status: 'CANCELLED' } : r));
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  arrived(): void {
    this.act(this.api.arrived(this.id));
  }

  start(): void {
    this.act(this.api.start(this.id));
  }

  complete(): void {
    this.act(this.api.complete(this.id));
  }

  private act(call: ReturnType<DriverRideService['arrived']>): void {
    this.busy.set(true);
    call.subscribe({
      next: (r) => {
        this.busy.set(false);
        this.ride.set(r);
      },
      error: (e) => {
        this.busy.set(false);
        this.toast.error(errorMessage(e));
      },
    });
  }
}
