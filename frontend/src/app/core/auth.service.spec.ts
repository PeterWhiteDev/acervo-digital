import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('AuthService role claims', () => {
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    });
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('exposes administrator role and collector id after login', () => {
    const service = TestBed.inject(AuthService);
    service.login({ email: 'admin@example.test', password: 'secret123' }).subscribe();
    httpTesting.expectOne('/api/v1/auth/token').flush({
      accessToken: jwt({ sub: 'admin-id', roles: ['ADMIN'] }),
      tokenType: 'Bearer',
      expiresInSeconds: 900,
    });

    expect(service.collectorId()).toBe('admin-id');
    expect(service.role()).toBe('ADMIN');
    expect(service.isAdmin()).toBe(true);
  });

  it('does not grant admin navigation to a regular user', () => {
    const service = TestBed.inject(AuthService);
    service.login({ email: 'user@example.test', password: 'secret123' }).subscribe();
    httpTesting.expectOne('/api/v1/auth/token').flush({
      accessToken: jwt({ sub: 'user-id', roles: ['USER'] }),
      tokenType: 'Bearer',
      expiresInSeconds: 900,
    });

    expect(service.collectorId()).toBe('user-id');
    expect(service.role()).toBe('USER');
    expect(service.isAdmin()).toBe(false);
  });

  it('clears an expired stored token and marks the session as expired', async () => {
    localStorage.setItem('acervo.access-token', jwt({
      sub: 'expired-user',
      roles: ['USER'],
      exp: Math.floor(Date.now() / 1000) - 1,
    }));
    const service = TestBed.inject(AuthService);

    await new Promise((resolve) => setTimeout(resolve, 0));

    expect(service.isAuthenticated()).toBe(false);
    expect(service.sessionExpired()).toBe(true);
    expect(localStorage.getItem('acervo.access-token')).toBeNull();
  });

  it('expires the session when a protected request returns 401', () => {
    localStorage.setItem('acervo.access-token', jwt({
      sub: 'expired-user',
      roles: ['USER'],
      exp: Math.floor(Date.now() / 1000) + 900,
    }));
    const service = TestBed.inject(AuthService);
    TestBed.inject(HttpClient).get('/api/v1/items').subscribe({ error: () => undefined });

    httpTesting.expectOne('/api/v1/items').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(service.sessionExpired()).toBe(true);
    expect(service.token()).toBeNull();
  });
});

function jwt(claims: Record<string, unknown>): string {
  const payload = btoa(JSON.stringify(claims))
    .replaceAll('+', '-')
    .replaceAll('/', '_')
    .replaceAll('=', '');
  return `header.${payload}.signature`;
}