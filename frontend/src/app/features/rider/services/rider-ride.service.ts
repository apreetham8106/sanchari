import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { BookRideBody, FareEstimate, PageResponse, Ride, VehicleType } from '../../../core/models/ride.model';

@Injectable({ providedIn: 'root' })
export class RiderRideService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/rides`;

  estimate(vehicleType: VehicleType, pickup: { lat: number; lng: number }, drop: { lat: number; lng: number }): Observable<FareEstimate> {
    return this.http.post<FareEstimate>(`${this.base}/estimate`, {
      vehicleType,
      pickupLat: pickup.lat,
      pickupLng: pickup.lng,
      dropLat: drop.lat,
      dropLng: drop.lng,
    });
  }

  book(body: BookRideBody): Observable<Ride> {
    return this.http.post<Ride>(this.base, body);
  }

  /** Emits null when there is no active ride (HTTP 204). */
  active(): Observable<Ride | null> {
    return this.http.get<Ride | null>(`${this.base}/active`);
  }

  get(id: string): Observable<Ride> {
    return this.http.get<Ride>(`${this.base}/${id}`);
  }

  cancel(id: string): Observable<Ride> {
    return this.http.post<Ride>(`${this.base}/${id}/cancel`, {});
  }

  history(page = 0, size = 20): Observable<PageResponse<Ride>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Ride>>(this.base, { params });
  }
}
