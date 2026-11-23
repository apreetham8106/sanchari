import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PageResponse, Ride, RideOffer } from '../../../core/models/ride.model';

@Injectable({ providedIn: 'root' })
export class DriverRideService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/driver`;

  offers(): Observable<RideOffer[]> {
    return this.http.get<RideOffer[]>(`${this.base}/offers`);
  }

  /** Emits null when the driver has no active ride (HTTP 204). */
  active(): Observable<Ride | null> {
    return this.http.get<Ride | null>(`${this.base}/rides/active`);
  }

  history(page = 0, size = 50): Observable<PageResponse<Ride>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Ride>>(`${this.base}/rides`, { params });
  }

  accept(id: string): Observable<Ride> {
    return this.http.post<Ride>(`${this.base}/rides/${id}/accept`, {});
  }

  reject(id: string): Observable<unknown> {
    return this.http.post(`${this.base}/rides/${id}/reject`, {});
  }

  arrived(id: string): Observable<Ride> {
    return this.http.post<Ride>(`${this.base}/rides/${id}/arrived`, {});
  }

  start(id: string): Observable<Ride> {
    return this.http.post<Ride>(`${this.base}/rides/${id}/start`, {});
  }

  complete(id: string): Observable<Ride> {
    return this.http.post<Ride>(`${this.base}/rides/${id}/complete`, {});
  }
}
