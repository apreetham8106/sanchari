import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { WebSocketService } from '../../core/realtime/websocket.service';

interface NavItem {
  label: string;
  path: string;
  exact?: boolean;
}

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="bar">
      <div class="inner">
        <a class="brand" [routerLink]="home()">Sanchari</a>
        <nav>
          @for (item of nav(); track item.path) {
            <a [routerLink]="item.path" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: !!item.exact }">{{ item.label }}</a>
          }
        </nav>
        <div class="who">
          <a routerLink="/profile" class="name">{{ auth.user()?.fullName }}</a>
          <button class="btn sm" (click)="logout()">Log out</button>
        </div>
      </div>
    </header>
    <router-outlet />
  `,
  styles: [
    `
      .bar { background: var(--ink); color: #fff; position: sticky; top: 0; z-index: 1000; }
      .inner { max-width: 1100px; margin: 0 auto; padding: 10px 16px; display: flex; align-items: center; gap: 20px; flex-wrap: wrap; }
      .brand { font-weight: 800; font-size: 1.25rem; letter-spacing: 0.02em; color: var(--accent); text-decoration: none; }
      nav { display: flex; gap: 4px; flex: 1; flex-wrap: wrap; }
      nav a { color: #cfd6ee; padding: 6px 12px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 0.92rem; }
      nav a.active { background: var(--ink-2); color: #fff; }
      .who { display: flex; align-items: center; gap: 10px; }
      .name { color: #fff; font-weight: 600; }
    `,
  ],
})
export class ShellComponent {
  readonly auth = inject(AuthService);
  private readonly ws = inject(WebSocketService);

  readonly home = computed(() => this.auth.homeRoute());

  readonly nav = computed<NavItem[]>(() => {
    switch (this.auth.role()) {
      case 'RIDER':
        return [
          { label: 'Book a ride', path: '/rider', exact: true },
          { label: 'My rides', path: '/rider/history' },
        ];
      case 'DRIVER':
        return [
          { label: 'Dashboard', path: '/driver', exact: true },
          { label: 'Onboarding', path: '/driver/onboarding' },
          { label: 'Earnings', path: '/driver/earnings' },
        ];
      case 'ADMIN':
        return [
          { label: 'Dashboard', path: '/admin', exact: true },
          { label: 'Drivers', path: '/admin/drivers' },
          { label: 'Users', path: '/admin/users' },
          { label: 'Rides', path: '/admin/rides' },
        ];
      default:
        return [];
    }
  });

  logout(): void {
    this.ws.disconnect();
    this.auth.logout();
  }
}
