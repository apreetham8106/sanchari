import { Injectable } from '@angular/core';
import { LatLng } from '../models/ride.model';

@Injectable({ providedIn: 'root' })
export class GeolocationService {
  getCurrent(): Promise<LatLng> {
    return new Promise((resolve, reject) => {
      if (!('geolocation' in navigator)) {
        reject(new Error('Location is not supported on this device'));
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (p) => resolve({ lat: p.coords.latitude, lng: p.coords.longitude }),
        () => reject(new Error('Location permission denied. Allow location access to continue.')),
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 5000 },
      );
    });
  }

  /** Returns a function that stops watching. */
  watch(onPosition: (p: LatLng, heading: number | null) => void): () => void {
    if (!('geolocation' in navigator)) {
      return () => undefined;
    }
    const id = navigator.geolocation.watchPosition(
      (p) => onPosition({ lat: p.coords.latitude, lng: p.coords.longitude }, p.coords.heading),
      () => undefined,
      { enableHighAccuracy: true, maximumAge: 2000 },
    );
    return () => navigator.geolocation.clearWatch(id);
  }
}
