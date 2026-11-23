import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { DriverLocationMessage, LatLng, Ride, RideStatus, RideStatusEvent } from '../../core/models/ride.model';
import { WebSocketService } from '../../core/realtime/websocket.service';
import { MapMarker, MapViewComponent } from '../../shared/components/map-view.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';
import { RiderRideService } from './services/rider-ride.service';

const STEPS: RideStatus[] = ['SEARCHING', 'ACCEPTED', 'ARRIVED', 'STARTED', 'COMPLETED'];

@Component({
  selector: 'app-ride-tracking',
  standalone: true,
  imports: [MapViewComponent, StatusBadgeComponent, CurrencyInrPipe, DatePipe, RouterLink],
  template: `
    <div class="page">
      @if (ride(); as r) {
        <div class="split">
          <div class="card">
            <div class="row between">
              <h2>{{ headline() }}</h2>
              <app-status-badge [status]="r.status" />
            </div>

            <div class="steps">
              @for (s of steps; track s; let i = $index) {
                <div class="step" [class.done]="i <= stepIndex()"></div>
              }
            </div>

            @switch (r.status) {
              @case ('SEARCHING') {
                <p class="pulse">Looking for drivers within {{ r.searchRadiusM / 1000 }} km…</p>
                <p class="small muted">We widen the search automatically if nobody nearby is free.</p>
              }
              @case ('NO_DRIVER') {
                <div class="alert warn">No driver accepted your ride. Please try again.</div>
              }
              @case ('CANCELLED') {
                <div class="alert warn">This ride was cancelled.</div>
              }
            }

            @if (r.driver; as d) {
              <div class="card flat mb">
                <b>{{ d.name }}</b>
                @if (d.rating) {
                  <span class="muted"> · ★ {{ d.rating }}</span>
                }
                <div class="small muted">{{ d.vehicleModel }} {{ d.vehicleRegistration }}</div>
                @if (d.phone) {
                  <a class="small" [href]="'tel:' + d.phone">Call {{ d.phone }}</a>
                }
              </div>
            }

            <div class="small mb">
              <div><b>From</b> {{ r.pickupAddress || 'Pinned location' }}</div>
              <div><b>To</b> {{ r.dropAddress || 'Pinned location' }}</div>
              <div class="muted">Booked {{ r.createdAt | date: 'shortTime' }}</div>
            </div>

            <div class="row between mb">
              <span class="muted">Fare</span>
              <b>{{ (r.finalFare ?? r.estimatedFare) | inr }}</b>
            </div>

            @if (canCancel()) {
              <button class="btn danger block" [disabled]="busy()" (click)="cancel()">Cancel ride</button>
            }
            @if (finished()) {
              <a class="btn primary block" routerLink="/rider">Book another ride</a>
            }
          </div>

          <div class="map-box tall">
            <app-map-view [center]="mapCenter()" [markers]="markers()" [fit]="true" />
          </div>
        </div>
      } @else {
        <p class="muted">Loading ride…</p>
      }
    </div>
  `,
})
export class RideTrackingComponent implements OnInit, OnDestroy {
  private readonly api = inject(RiderRideService);
  private readonly ws = inject(WebSocketService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly steps = STEPS;
  readonly ride = signal<Ride | null>(null);
  readonly driverPos = signal<LatLng | null>(null);
  readonly busy = signal(false);

  private id = '';
  private subs = new Subscription();

  readonly stepIndex = computed(() => {
    const s = this.ride()?.status;
    const i = s ? STEPS.indexOf(s) : -1;
    return i < 0 ? 0 : i;
  });

  readonly canCancel = computed(() => {
    const s = this.ride()?.status;
    return s === 'SEARCHING' || s === 'ACCEPTED' || s === 'ARRIVED';
  });

  readonly finished = computed(() => {
    const s = this.ride()?.status;
    return s === 'COMPLETED' || s === 'CANCELLED' || s === 'NO_DRIVER';
  });

  readonly headline = computed(() => {
    switch (this.ride()?.status) {
      case 'SEARCHING':
        return 'Finding your driver';
      case 'ACCEPTED':
        return 'Driver is on the way';
      case 'ARRIVED':
        return 'Your driver has arrived';
      case 'STARTED':
        return 'Ride in progress';
      case 'COMPLETED':
        return 'You have arrived';
      case 'CANCELLED':
        return 'Ride cancelled';
      default:
        return 'No driver found';
    }
  });

  readonly mapCenter = computed<LatLng>(() => {
    const r = this.ride();
    return r ? { lat: r.pickupLat, lng: r.pickupLng } : { lat: 17.385, lng: 78.4867 };
  });

  readonly markers = computed<MapMarker[]>(() => {
    const r = this.ride();
    if (!r) {
      return [];
    }
    const list: MapMarker[] = [
      { id: 'pickup', lat: r.pickupLat, lng: r.pickupLng, label: 'Pickup', color: '#138a55' },
      { id: 'drop', lat: r.dropLat, lng: r.dropLng, label: 'Drop', color: '#c0283f' },
    ];
    const d = this.driverPos();
    if (d && (r.status === 'ACCEPTED' || r.status === 'ARRIVED' || r.status === 'STARTED')) {
      list.push({ id: 'driver', lat: d.lat, lng: d.lng, label: 'Driver', color: '#2158c4' });
    }
    return list;
  });

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id') ?? '';
    this.load();
    this.ws.connect();
    this.subs.add(
      this.ws.watch<RideStatusEvent>(`/topic/rides/${this.id}`).subscribe(() => this.load()),
    );
    this.subs.add(
      this.ws.watch<DriverLocationMessage>(`/topic/rides/${this.id}/driver-location`).subscribe((m) => this.driverPos.set({ lat: m.lat, lng: m.lng })),
    );
    // Safety net in case a WebSocket frame is missed.
    this.subs.add(interval(10000).subscribe(() => !this.finished() && this.load()));
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  cancel(): void {
    if (!confirm('Cancel this ride?')) {
      return;
    }
    this.busy.set(true);
    this.api.cancel(this.id).subscribe({
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

  private load(): void {
    this.api.get(this.id).subscribe({
      next: (r) => this.ride.set(r),
      error: (e) => {
        this.toast.error(errorMessage(e, 'Could not load the ride'));
        void this.router.navigate(['/rider']);
      },
    });
  }
}
