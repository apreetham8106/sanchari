import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AdminDriver, DashboardStats, KycStatus } from '../../../core/models/driver.model';
import { PageResponse, Ride, RideStatus } from '../../../core/models/ride.model';
import { MessageResponse, Role, User } from '../../../core/models/user.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin`;

  dashboard(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.base}/dashboard`);
  }

  drivers(kycStatus: KycStatus | '', page = 0, size = 20): Observable<PageResponse<AdminDriver>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (kycStatus) {
      params = params.set('kycStatus', kycStatus);
    }
    return this.http.get<PageResponse<AdminDriver>>(`${this.base}/drivers`, { params });
  }

  reviewDriver(id: string, approve: boolean, remarks?: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.base}/drivers/${id}/kyc-review`, { approve, remarks });
  }

  reviewVehicle(id: string, approve: boolean, remarks?: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.base}/vehicles/${id}/review`, { approve, remarks });
  }

  users(role: Role | '', q: string, page = 0, size = 20): Observable<PageResponse<User>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (role) {
      params = params.set('role', role);
    }
    if (q.trim()) {
      params = params.set('q', q.trim());
    }
    return this.http.get<PageResponse<User>>(`${this.base}/users`, { params });
  }

  setBlocked(id: string, blocked: boolean): Observable<User> {
    return this.http.post<User>(`${this.base}/users/${id}/${blocked ? 'block' : 'unblock'}`, {});
  }

  rides(status: RideStatus | '', page = 0, size = 20): Observable<PageResponse<Ride>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PageResponse<Ride>>(`${this.base}/rides`, { params });
  }
}
