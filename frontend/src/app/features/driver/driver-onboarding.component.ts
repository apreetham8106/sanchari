import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { errorMessage } from '../../core/http/error.util';
import { ToastService } from '../../core/http/toast.service';
import { DriverProfile, KycEvent } from '../../core/models/driver.model';
import { VehicleType } from '../../core/models/ride.model';
import { WebSocketService } from '../../core/realtime/websocket.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { DriverService } from './services/driver.service';

@Component({
  selector: 'app-driver-onboarding',
  standalone: true,
  imports: [FormsModule, RouterLink, StatusBadgeComponent],
  template: `
    <div class="page">
      <h1>Driver onboarding</h1>
      @if (devHints) {
        <div class="alert info">
          Dev mode: verification is simulated. Approve it from an admin account, or call
          <code>POST /api/dev/kyc/&#123;requestId&#125;/approved</code> (the request id is in the backend log).
        </div>
      }

      @if (profile(); as p) {
        <div class="grid cols-2">
          <div class="card">
            <div class="row between">
              <h2>1. Identity (Digio)</h2>
              <app-status-badge [status]="p.kycStatus" />
            </div>

            @switch (p.kycStatus) {
              @case ('VERIFIED') {
                <div class="alert ok">Verified. Licence {{ p.licenseNumber }}, valid until {{ p.licenseExpiry }}.</div>
              }
              @case ('IN_PROGRESS') {
                <div class="alert info">Verification in progress. This page updates by itself when it finishes.</div>
              }
              @default {
                @if (p.kycStatus === 'REJECTED') {
                  <div class="alert bad">Verification was rejected. Check your details and submit again.</div>
                }
                <label class="field">Driving licence number<input name="lic" [(ngModel)]="licenseNumber" /></label>
                <label class="field">Licence expiry<input type="date" name="exp" [(ngModel)]="licenseExpiry" /></label>
                <label class="field">Date of birth<input type="date" name="dob" [(ngModel)]="dateOfBirth" /></label>
                <button class="btn primary" [disabled]="busy() || !licenseNumber || !licenseExpiry || !dateOfBirth" (click)="submitKyc()">
                  Submit for verification
                </button>
              }
            }
          </div>

          <div class="card">
            <h2>2. Vehicles</h2>
            @for (v of p.vehicles; track v.id) {
              <div class="row between mb">
                <div>
                  <b>{{ v.registrationNo }}</b> <span class="muted small">{{ v.vehicleType }} {{ v.makeModel }}</span>
                </div>
                <app-status-badge [status]="v.rcStatus" />
              </div>
            }
            @if (p.vehicles.length === 0) {
              <p class="muted">No vehicles yet.</p>
            }

            <hr style="border: 0; border-top: 1px solid var(--line); margin: 12px 0" />
            <h3>Add a vehicle</h3>
            <label class="field">Registration number<input name="reg" [(ngModel)]="registrationNo" placeholder="TS09AB1234" /></label>
            <label class="field">
              Type
              <select name="vt" [(ngModel)]="vehicleType">
                <option value="BIKE">Bike</option>
                <option value="AUTO">Auto</option>
                <option value="CAB">Cab</option>
                <option value="SUV">SUV</option>
              </select>
            </label>
            <label class="field">Make and model (optional)<input name="mm" [(ngModel)]="makeModel" /></label>
            <button class="btn primary" [disabled]="busy() || !registrationNo" (click)="addVehicle()">Add and verify</button>
          </div>
        </div>

        @if (p.kycStatus === 'VERIFIED' && hasVerifiedVehicle(p)) {
          <div class="alert ok mt">You are ready to drive. <a routerLink="/driver">Go to the dashboard</a> and go online.</div>
        }
      } @else {
        <p class="muted">Loading…</p>
      }
    </div>
  `,
})
export class DriverOnboardingComponent implements OnInit, OnDestroy {
  private readonly api = inject(DriverService);
  private readonly ws = inject(WebSocketService);
  private readonly toast = inject(ToastService);

  readonly devHints = environment.devHints;
  readonly profile = signal<DriverProfile | null>(null);
  readonly busy = signal(false);

  licenseNumber = '';
  licenseExpiry = '';
  dateOfBirth = '';
  registrationNo = '';
  vehicleType: VehicleType = 'CAB';
  makeModel = '';

  private sub?: Subscription;

  ngOnInit(): void {
    this.load();
    this.ws.connect();
    this.sub = this.ws.watch<KycEvent>('/user/queue/kyc').subscribe((e) => {
      this.toast.show(`${e.subject === 'DRIVER' ? 'Identity' : 'Vehicle'} verification: ${e.status.replace(/_/g, ' ').toLowerCase()}`);
      this.load();
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  hasVerifiedVehicle(p: DriverProfile): boolean {
    return p.vehicles.some((v) => v.rcStatus === 'VERIFIED');
  }

  submitKyc(): void {
    this.busy.set(true);
    this.api
      .startKyc({ licenseNumber: this.licenseNumber.trim(), licenseExpiry: this.licenseExpiry, dateOfBirth: this.dateOfBirth })
      .subscribe({
        next: (p) => {
          this.busy.set(false);
          this.profile.set(p);
          this.toast.ok('Submitted for verification');
        },
        error: (e) => {
          this.busy.set(false);
          this.toast.error(errorMessage(e));
        },
      });
  }

  addVehicle(): void {
    this.busy.set(true);
    this.api
      .addVehicle({ registrationNo: this.registrationNo.trim(), vehicleType: this.vehicleType, makeModel: this.makeModel.trim() || undefined })
      .subscribe({
        next: () => {
          this.busy.set(false);
          this.registrationNo = '';
          this.makeModel = '';
          this.toast.ok('Vehicle added and sent for verification');
          this.load();
        },
        error: (e) => {
          this.busy.set(false);
          this.toast.error(errorMessage(e));
        },
      });
  }

  private load(): void {
    this.api.profile().subscribe({
      next: (p) => this.profile.set(p),
      error: (e) => this.toast.error(errorMessage(e)),
    });
  }
}
