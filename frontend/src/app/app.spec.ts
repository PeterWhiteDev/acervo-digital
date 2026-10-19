import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();
  });

  it('creates the application shell', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
    expect(fixture.nativeElement.querySelector('router-outlet')).toBeTruthy();
  });

  it('orders the main sections with collections first and shows users only to admins', () => {
    localStorage.setItem('acervo.access-token', tokenWithRole('ADMIN'));
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const root = fixture.nativeElement as HTMLElement;
    const links = Array.from(root.querySelectorAll<HTMLAnchorElement>('.primary-nav a'))
      .map((link) => link.textContent?.replace(/\s+/g, '').trim());
    expect(links).toEqual(['01Colecciones', '02Catálogo', '03Tiposyatributos', '04Usuarios']);
  });

  it('hides the user section from regular accounts', () => {
    localStorage.setItem('acervo.access-token', tokenWithRole('USER'));
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const root = fixture.nativeElement as HTMLElement;
    const links = Array.from(root.querySelectorAll<HTMLAnchorElement>('.primary-nav a'))
      .map((link) => link.textContent ?? '');
    expect(links.join(' ')).not.toContain('Usuarios');
  });
});

function tokenWithRole(role: 'USER' | 'ADMIN'): string {
  const payload = btoa(JSON.stringify({ sub: 'collector-1', roles: [role] }))
    .replaceAll('+', '-')
    .replaceAll('/', '_')
    .replaceAll('=', '');
  return `header.${payload}.signature`;
}
