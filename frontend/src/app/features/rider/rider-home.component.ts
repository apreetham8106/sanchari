import { Component, OnInit, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { GeolocationService } from '../../core/geo/geolocation.service';
import { FareEstimate, LatLng, VehicleType } from '../../core/models/ride.model';
import { MapMarker, MapViewComponent } from '../../shared/components/map-view.component';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';
import { RiderRideService } from './services/rider-ride.service';

@Component({
  selector: 'app-rider-home',
  standalone: true,
  imports: [FormsModule, MapViewComponent, CurrencyInrPipe],
  template: `
    <div class="page">
      <div class="split">
        <div class="card">
          <h2>Where to?</h2>
          <p class="muted small">Tap the map to set the {{ mode() === 'pickup' ? 'pickup' : 'drop' }} point.</p>

          <div class="tabs">
            <button type="button" [class.active]="mode() === 'pickup'" (click)="mode.set('pickup')">Pickup</button>
            <button type="button" [class.active]="mode() === 'drop'" (click)="mode.set('drop')">Drop</button>
          </div>

          <label class="field">
            Pickup {{ pickup() ? '✓' : '' }}
            <input name="pickupAddress" [(ngModel)]="pickupAddress" placeholder="Label, e.g. Home (optional)" />
          </label>
          <label class="field">
            Drop {{ drop() ? '✓' : '' }}
            <input name="dropAddress" [(ngModel)]="dropAddress" placeholder="Label, e.g. Office (optional)" />
          </label>
          <button class="btn sm mb" (click)="useMyLocation()">Use my location as pickup</button>

          <div class="vehicle-pick mb">
            @for (v of vehicles; track v.type) {
              <button type="button" [class.selected]="vehicleType() === v.type" (click)="vehicleType.set(v.type)">
                <span class="emoji">{{ v.icon }}</span>{{ v.label }}
              </button>
            }
          </div>

          @if (estimate(); as e) {
            <div class="alert info">
              <b>{{ e.estimatedFare | inr }}</b> · about {{ e.distanceKm }} km
            </div>
          }

          <button class="btn primary block" [disabled]="busy() || !pickup() || !drop()" (click)="book()">
            {{ busy() ? 'Booking…' : 'Book ' + vehicleLabel() }}
          </button>
        </div>

        <div class="map-box tall">
          <app-map-view [center]="center()" [markers]="markers()" [selectable]="true" [fit]="false" (mapClick)="onMapClick($event)" />
        </div>
      </div>
    </div>
  `,
})
export class RiderHomeComponent implements OnInit {
  private readonly api = inject(RiderRideService);
  private readonly geo = inject(GeolocationService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly vehicles: { type: VehicleType; label: string; icon: string }[] = [
    { type: 'BIKE', label: 'Bike', icon: '🏍️' },
    { type: 'AUTO', label: 'Auto', icon: '🛺' },
    { type: 'CAB', label: 'Cab', icon: '🚕' },
    { type: 'SUV', label: 'SUV', icon: '🚙' },
  ];

  readonly center = signal<LatLng>(environment.defaultCenter);
  readonly pickup = signal<LatLng | null>(null);
  readonly drop = signal<LatLng | null>(null);
  readonly mode = signal<'pickup' | 'drop'>('pickup');
  readonly vehicleType = signal<VehicleType>('CAB');
  readonly estimate = signal<FareEstimate | null>(null);
  readonly busy = signal(false);

  pickupAddress = '';
  dropAddress = '';

  readonly vehicleLabel = computed(() => this.vehicles.find((v) => v.type === this.vehicleType())?.label ?? '');

  readonly markers = computed<MapMarker[]>(() => {
    const list: MapMarker[] = [];
    const p = this.pickup();
    const d = this.drop();
    if (p) {
      list.push({ id: 'pickup', lat: p.lat, lng: p.lng, label: 'Pickup', color: '#138a55' });
    }
    if (d) {
      list.push({ id: 'drop', lat: d.lat, lng: d.lng, label: 'Drop', color: '#c0283f' });
    }
    return list;
  });

  constructor() {
    effect(() => {
      const p = this.pickup();
      const d = this.drop();
      const v = this.vehicleType();
      untracked(() => this.refreshEstimate(p, d, v));
    });
  }

  ngOnInit(): void {
    this.api.active().subscribe({
      next: (ride) => {
        if (ride) {
          void this.router.navigate(['/rider/ride', ride.id]);
        }
      },
      error: () => undefined,
    });
    this.useMyLocation(true);
  }

  useMyLocation(silent = false): void {
    this.geo
      .getCurrent()
      .then((pos) => {
        this.center.set(pos);
        this.pickup.set(pos);
        this.mode.set('drop');
      })
      .catch((e: Error) => {
        if (!silent) {
          this.toast.error(e.message);
        }
      });
  }

  onMapClick(p: LatLng): void {
    if (this.mode() === 'pickup') {
      this.pickup.set(p);
      this.mode.set('drop');
    } else {
      this.drop.set(p);
    }
  }

  book(): void {
    const p = this.pickup();
    const d = this.drop();
    if (!p || !d || this.busy()) {
      return;
    }
    this.busy.set(true);
    this.api
      .book({
        vehicleType: this.vehicleType(),
        pickupLat: p.lat,
        pickupLng: p.lng,
        pickupAddress: this.pickupAddress.trim() || 'Pinned location',
        dropLat: d.lat,
        dropLng: d.lng,
        dropAddress: this.dropAddress.trim() || 'Pinned location',
      })
      .subscribe({
        next: (ride) => void this.router.navigate(['/rider/ride', ride.id]),
        error: (e) => {
          this.busy.set(false);
          this.toast.error(errorMessage(e));
        },
      });
  }

  private refreshEstimate(p: LatLng | null, d: LatLng | null, v: VehicleType): void {
    if (!p || !d) {
      this.estimate.set(null);
      return;
    }
    this.api.estimate(v, p, d).subscribe({
      next: (e) => this.estimate.set(e),
      error: () => this.estimate.set(null),
    });
  }
}
