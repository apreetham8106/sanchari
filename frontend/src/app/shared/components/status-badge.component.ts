import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `<span class="badge" [class]="'badge ' + tone">{{ label }}</span>`,
})
export class StatusBadgeComponent {
  @Input({ required: true }) status = '';

  get label(): string {
    return this.status.replace(/_/g, ' ');
  }

  get tone(): string {
    switch (this.status) {
      case 'VERIFIED':
      case 'COMPLETED':
      case 'ACTIVE':
      case 'ACCEPTED':
        return 'ok';
      case 'IN_PROGRESS':
      case 'PENDING':
      case 'SEARCHING':
      case 'ARRIVED':
      case 'STARTED':
        return 'info';
      case 'REJECTED':
      case 'CANCELLED':
      case 'NO_DRIVER':
      case 'BLOCKED':
        return 'bad';
      default:
        return '';
    }
  }
}
