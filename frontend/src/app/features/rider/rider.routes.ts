import { Routes } from '@angular/router';

export const RIDER_ROUTES: Routes = [
  { path: '', pathMatch: 'full', loadComponent: () => import('./rider-home.component').then((m) => m.RiderHomeComponent) },
  { path: 'ride/:id', loadComponent: () => import('./ride-tracking.component').then((m) => m.RideTrackingComponent) },
  { path: 'history', loadComponent: () => import('./ride-history.component').then((m) => m.RideHistoryComponent) },
];
