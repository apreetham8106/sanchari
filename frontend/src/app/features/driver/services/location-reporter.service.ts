import { Injectable, inject } from '@angular/core';
import { GeolocationService } from '../../../core/geo/geolocation.service';
import { LatLng } from '../../../core/models/ride.model';
import { DriverService } from './driver.service';

/**
 * Streams the driver's position to the backend while online. The backend only matches drivers whose last
 * position is under ~30 s old, so a heartbeat re-sends the last known point even when the driver is standing still.
 */
@Injectable({ providedIn: 'root' })
export class LocationReporterService {
  private readonly geo = inject(GeolocationService);
  private readonly driver = inject(DriverService);

  private stopWatch?: () => void;
  private heartbeat?: ReturnType<typeof setInterval>;
  private last: { pos: LatLng; heading: number | null } | null = null;
  private lastSent = 0;

  get running(): boolean {
    return !!this.stopWatch;
  }

  start(initial?: LatLng): void {
    if (this.stopWatch) {
      return;
    }
    if (initial) {
      this.last = { pos: initial, heading: null };
    }
    this.stopWatch = this.geo.watch((pos, heading) => {
      this.last = { pos, heading };
      if (Date.now() - this.lastSent >= 4000) {
        this.send();
      }
    });
    this.heartbeat = setInterval(() => {
      if (Date.now() - this.lastSent >= 8000) {
        this.send();
      }
    }, 4000);
    this.send();
  }

  stop(): void {
    this.stopWatch?.();
    this.stopWatch = undefined;
    if (this.heartbeat) {
      clearInterval(this.heartbeat);
      this.heartbeat = undefined;
    }
    this.last = null;
  }

  private send(): void {
    if (!this.last) {
      return;
    }
    this.lastSent = Date.now();
    this.driver.sendLocation(this.last.pos.lat, this.last.pos.lng, this.last.heading).subscribe({ error: () => undefined });
  }
}
