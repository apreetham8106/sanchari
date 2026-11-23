export type VehicleType = 'BIKE' | 'AUTO' | 'CAB' | 'SUV';
export type RideStatus = 'SEARCHING' | 'ACCEPTED' | 'ARRIVED' | 'STARTED' | 'COMPLETED' | 'CANCELLED' | 'NO_DRIVER';

export interface LatLng {
  lat: number;
  lng: number;
}

export interface DriverSummary {
  id: string;
  name?: string;
  phone?: string;
  rating?: number;
  vehicleRegistration?: string;
  vehicleType?: VehicleType;
  vehicleModel?: string;
}

export interface Ride {
  id: string;
  riderId: string;
  status: RideStatus;
  vehicleType: VehicleType;
  pickupLat: number;
  pickupLng: number;
  pickupAddress?: string;
  dropLat: number;
  dropLng: number;
  dropAddress?: string;
  estimatedFare?: number;
  finalFare?: number;
  searchRadiusM: number;
  driver?: DriverSummary;
  createdAt: string;
  acceptedAt?: string;
  completedAt?: string;
}

export interface BookRideBody {
  vehicleType: VehicleType;
  pickupLat: number;
  pickupLng: number;
  pickupAddress?: string;
  dropLat: number;
  dropLng: number;
  dropAddress?: string;
}

export interface FareEstimate {
  vehicleType: VehicleType;
  distanceKm: number;
  estimatedFare: number;
  currency: string;
}

export interface RideOffer {
  rideId: string;
  radiusM: number;
  expiresAt: string;
  pickupAddress?: string;
  pickupLat: number;
  pickupLng: number;
  dropAddress?: string;
  dropLat: number;
  dropLng: number;
  estimatedFare: number;
  distanceToPickupM: number;
}

export interface RideStatusEvent {
  rideId: string;
  riderId: string;
  driverId?: string;
  status: RideStatus;
  at: string;
}

export interface DriverLocationMessage {
  rideId: string;
  driverId: string;
  lat: number;
  lng: number;
  heading?: number;
  ts: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
