import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { DriverProfile, StartKycBody, Vehicle } from '../../../core/models/driver.model';
import { VehicleType } from '../../../core/models/ride.model';

@Injectable({ providedIn: 'root' })
export class DriverService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/driver`;

  profile(): Observable<DriverProfile> {
    return this.http.get<DriverProfile>(`${this.base}/profile`);
  }

  startKyc(body: StartKycBody): Observable<DriverProfile> {
    return this.http.post<DriverProfile>(`${this.base}/kyc`, body);
  }

  addVehicle(body: { registrationNo: string; vehicleType: VehicleType; makeModel?: string }): Observable<Vehicle> {
    return this.http.post<Vehicle>(`${this.base}/vehicles`, body);
  }

  setOnline(online: boolean, lat?: number, lng?: number): Observable<DriverProfile> {
    return this.http.post<DriverProfile>(`${this.base}/online`, { online, lat, lng });
  }

  sendLocation(lat: number, lng: number, heading?: number | null): Observable<unknown> {
    return this.http.post(`${this.base}/location`, { lat, lng, heading: heading ?? null });
  }
}
