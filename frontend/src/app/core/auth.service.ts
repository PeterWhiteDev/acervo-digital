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

  readonly token = this.tokenState.asReadonly();
  readonly email = this.emailState.asReadonly();
  readonly isAuthenticated = computed(() => Boolean(this.tokenState()));

  register(request: { email: string; password: string; displayName: string }) {
    return this.http.post<Collector>('/api/v1/auth/register', request);
  }

  login(request: { email: string; password: string }) {
    return this.http.post<TokenResponse>('/api/v1/auth/token', request).pipe(
      tap((response) => {
        localStorage.setItem(TOKEN_KEY, response.accessToken);
        localStorage.setItem(EMAIL_KEY, request.email);
        this.tokenState.set(response.accessToken);
        this.emailState.set(request.email);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(EMAIL_KEY);
    this.tokenState.set(null);
    this.emailState.set('');
  }
}