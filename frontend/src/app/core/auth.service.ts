import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import { Collector, TokenResponse } from './models';

const TOKEN_KEY = 'acervo.access-token';
const EMAIL_KEY = 'acervo.email';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenState = signal(localStorage.getItem(TOKEN_KEY));
  private readonly emailState = signal(localStorage.getItem(EMAIL_KEY) ?? '');
  private expirationTimer: ReturnType<typeof setTimeout> | null = null;

  readonly token = this.tokenState.asReadonly();
  readonly email = this.emailState.asReadonly();
  readonly isAuthenticated = computed(() => Boolean(this.tokenState()));
  readonly sessionExpired = signal(false);
  private readonly claims = computed(() => decodeClaims(this.tokenState()));
  readonly collectorId = computed(() => {
    const subject = this.claims()?.['sub'];
    return typeof subject === 'string' ? subject : null;
  });
  readonly role = computed<'USER' | 'ADMIN' | null>(() => {
    const roles = this.claims()?.['roles'];
    if (!Array.isArray(roles)) return null;
    if (roles.includes('ADMIN')) return 'ADMIN';
    return roles.includes('USER') ? 'USER' : null;
  });
  readonly isAdmin = computed(() => this.role() === 'ADMIN');

  constructor() {
    this.scheduleExpiration(this.tokenState());
  }

  register(request: { email: string; password: string; displayName: string }) {
    return this.http.post<Collector>('/api/v1/auth/register', request);
  }

  login(request: { email: string; password: string }) {
    return this.http.post<TokenResponse>('/api/v1/auth/token', request).pipe(
      tap((response) => {
        this.sessionExpired.set(false);
        localStorage.setItem(TOKEN_KEY, response.accessToken);
        localStorage.setItem(EMAIL_KEY, request.email);
        this.tokenState.set(response.accessToken);
        this.emailState.set(request.email);
        this.scheduleExpiration(response.accessToken);
      }),
    );
  }

  logout(): void {
    this.clearExpirationTimer();
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(EMAIL_KEY);
    this.tokenState.set(null);
    this.emailState.set('');
    this.sessionExpired.set(false);
  }

  expireSession(): void {
    this.clearExpirationTimer();
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(EMAIL_KEY);
    this.tokenState.set(null);
    this.emailState.set('');
    this.sessionExpired.set(true);
  }

  private scheduleExpiration(token: string | null): void {
    this.clearExpirationTimer();
    const expiration = decodeClaims(token)?.['exp'];
    if (typeof expiration !== 'number') return;

    const remaining = Math.max(0, expiration * 1000 - Date.now());
    this.expirationTimer = setTimeout(() => this.expireSession(), Math.min(remaining, 2_147_000_000));
  }

  private clearExpirationTimer(): void {
    if (this.expirationTimer !== null) {
      clearTimeout(this.expirationTimer);
      this.expirationTimer = null;
    }
  }
}

function decodeClaims(token: string | null): Record<string, unknown> | null {
  if (!token) return null;
  try {
    const segment = token.split('.')[1]?.replaceAll('-', '+').replaceAll('_', '/');
    if (!segment) return null;
    return JSON.parse(atob(segment.padEnd(Math.ceil(segment.length / 4) * 4, '='))) as Record<string, unknown>;
  } catch {
    return null;
  }
}