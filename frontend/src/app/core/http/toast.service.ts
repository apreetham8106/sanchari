import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  text: string;
  kind: 'info' | 'ok' | 'bad';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private seq = 0;
  readonly toasts = signal<Toast[]>([]);

  show(text: string, kind: Toast['kind'] = 'info', ms = 4000): void {
    const id = ++this.seq;
    this.toasts.update((list) => [...list, { id, text, kind }]);
    setTimeout(() => this.dismiss(id), ms);
  }

  ok(text: string): void {
    this.show(text, 'ok');
  }

  error(text: string): void {
    this.show(text, 'bad', 6000);
  }

  dismiss(id: number): void {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }
}
