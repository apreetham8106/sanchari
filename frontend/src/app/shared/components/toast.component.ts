import { Component, inject } from '@angular/core';
import { ToastService } from '../../core/http/toast.service';

@Component({
  selector: 'app-toasts',
  standalone: true,
  template: `
    <div class="stack">
      @for (t of toasts.toasts(); track t.id) {
        <div class="toast" [class]="'toast ' + t.kind" (click)="toasts.dismiss(t.id)">{{ t.text }}</div>
      }
    </div>
  `,
  styles: [
    `
      .stack { position: fixed; right: 16px; bottom: 16px; display: flex; flex-direction: column; gap: 8px; z-index: 9999; max-width: min(380px, calc(100vw - 32px)); }
      .toast { padding: 12px 14px; border-radius: 10px; color: #fff; background: #14213d; box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25); cursor: pointer; font-size: 0.92rem; }
      .toast.ok { background: #138a55; }
      .toast.bad { background: #c0283f; }
    `,
  ],
})
export class ToastComponent {
  readonly toasts = inject(ToastService);
}
