import { Injectable } from '@angular/core';
import { User } from '../models/user.model';

const ACCESS = 'sanchari.access';
const REFRESH = 'sanchari.refresh';
const USER = 'sanchari.user';

/** localStorage wrapper that never throws (private windows, blocked storage). */
@Injectable({ providedIn: 'root' })
export class TokenStorage {
  private memory = new Map<string, string>();

  private get(key: string): string | null {
    try {
      return localStorage.getItem(key) ?? this.memory.get(key) ?? null;
    } catch {
      return this.memory.get(key) ?? null;
    }
  }

  private set(key: string, value: string): void {
    this.memory.set(key, value);
    try {
      localStorage.setItem(key, value);
    } catch {
      /* in-memory fallback only */
    }
  }

  private remove(key: string): void {
    this.memory.delete(key);
    try {
      localStorage.removeItem(key);
    } catch {
      /* ignore */
    }
  }

  access(): string | null {
    return this.get(ACCESS);
  }

  refresh(): string | null {
    return this.get(REFRESH);
  }

  user(): User | null {
    const raw = this.get(USER);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as User;
    } catch {
      return null;
    }
  }

  setTokens(access: string, refresh: string): void {
    this.set(ACCESS, access);
    this.set(REFRESH, refresh);
  }

  setUser(user: User): void {
    this.set(USER, JSON.stringify(user));
  }

  clear(): void {
    this.remove(ACCESS);
    this.remove(REFRESH);
    this.remove(USER);
  }
}
