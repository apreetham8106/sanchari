import { Injectable, inject } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';

/**
 * One shared STOMP connection. The JWT goes in the CONNECT frame (browsers cannot set headers on a
 * WebSocket handshake), and is re-read on every reconnect so a refreshed token is picked up.
 */
@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private readonly auth = inject(AuthService);
  private client?: Client;
  private readonly connected$ = new BehaviorSubject<boolean>(false);

  connect(): void {
    if (this.client?.active) {
      return;
    }
    this.client = new Client({
      brokerURL: environment.wsUrl,
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      beforeConnect: (client) => {
        client.connectHeaders = { Authorization: `Bearer ${this.auth.accessToken() ?? ''}` };
      },
      onConnect: () => this.connected$.next(true),
      onWebSocketClose: () => this.connected$.next(false),
      onStompError: () => this.connected$.next(false),
    });
    this.client.activate();
  }

  disconnect(): void {
    const c = this.client;
    this.client = undefined;
    this.connected$.next(false);
    if (c) {
      void c.deactivate();
    }
  }

  /** Emits each message body (parsed JSON) for a destination; resubscribes after reconnects. */
  watch<T>(destination: string): Observable<T> {
    return new Observable<T>((subscriber) => {
      let sub: StompSubscription | undefined;
      const stop = this.connected$.subscribe((up) => {
        sub?.unsubscribe();
        sub = undefined;
        if (up && this.client?.connected) {
          sub = this.client.subscribe(destination, (msg) => {
            try {
              subscriber.next(JSON.parse(msg.body) as T);
            } catch {
              /* ignore malformed frame */
            }
          });
        }
      });
      return () => {
        stop.unsubscribe();
        sub?.unsubscribe();
      };
    });
  }

  publish(destination: string, body: unknown): void {
    if (this.client?.connected) {
      this.client.publish({ destination, body: JSON.stringify(body) });
    }
  }
}
