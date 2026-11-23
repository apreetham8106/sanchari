import { VehicleType } from './ride.model';

export type KycStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'VERIFIED' | 'REJECTED';
export type RcStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';

export interface Vehicle {
  id: string;
  registrationNo: string;
  vehicleType: VehicleType;
  makeModel?: string;
  rcStatus: RcStatus;
}

export interface DriverProfile {
  userId: string;
  kycStatus: KycStatus;
  licenseNumber?: string;
  licenseExpiry?: string;
  online: boolean;
  available: boolean;
  rating: number;
  vehicles: Vehicle[];
}

export interface StartKycBody {
  licenseNumber: string;
  licenseExpiry: string;
  dateOfBirth: string;
}

export interface AdminDriver {
  userId: string;
  fullName?: string;
  phone?: string;
  kycStatus: KycStatus;
  licenseNumber?: string;
  licenseExpiry?: string;
  online: boolean;
  vehicles: Vehicle[];
}

export interface DashboardStats {
  totalRiders: number;
  totalDrivers: number;
  onlineDrivers: number;
  pendingKyc: number;
  ridesToday: number;
  activeRides: number;
  completedRides: number;
  cancelledRides: number;
}

export interface KycEvent {
  driverId: string;
  subject: 'DRIVER' | 'VEHICLE';
  status: string;
  refId?: string;
}
