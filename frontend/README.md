# sanchari-web

Angular 19 front end for Sanchari (standalone components, signals, lazy-loaded routes, Leaflet maps, STOMP over WebSocket).

```bash
npm install
npm start          # dev server on http://localhost:4200
npm run build      # production build in dist/sanchari-web/browser
```

## Configuration
`src/environments/environment.ts` (dev) and `environment.prod.ts` (prod build):

| Key | Dev | Prod |
|---|---|---|
| `apiUrl` | `http://localhost:8080/api` | `/api` (same origin, behind a reverse proxy) |
| `wsUrl` | `ws://localhost:8080/ws` | `/ws` |
| `devHints` | `true` (shows "OTP is 123456") | `false` |

For production, serve the built files and proxy `/api` and `/ws` (with WebSocket upgrade) to the backend, and
set the backend's `CORS_ORIGINS` if the front end is on another origin.

## Structure
```
src/app
├── core/       auth (service, token storage, interceptor, guards), http (errors, toasts),
│               realtime (STOMP service), geo (browser location), models
├── shared/     layout/shell, components (map-view, status-badge, otp-input, toasts), pipes (INR)
└── features/
    ├── auth/     login (password + OTP), register, otp-verify, reset-password
    ├── rider/    home (map, fare estimate, booking), ride-tracking (live), ride-history
    ├── driver/   dashboard (online toggle, live offers), onboarding (Digio KYC + vehicles),
    │             active-ride, earnings, services (incl. location reporter)
    ├── admin/    dashboard, drivers (KYC review), users (block), rides
    └── profile/
```
Routes are guarded by role: `/rider/**` for riders, `/driver/**` for drivers, `/admin/**` for admins.
The JWT interceptor attaches the access token and, on a 401, refreshes it once and retries.

## Notes
- Maps use OpenStreetMap tiles. Heavy production use needs a tile provider that permits it.
- Addresses are free-text labels plus a pinned map point. Add a geocoding/places API for address search.
- A driver's browser must stay open (and location allowed) while online; a native app would be better for that.
