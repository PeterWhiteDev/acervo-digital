import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.clear();
    delete document.documentElement.dataset['theme'];
  });

  it('uses the classic theme when nothing valid is stored', () => {
    localStorage.setItem('acervo.theme', 'unknown');
    const service = TestBed.inject(ThemeService);

    expect(service.theme()).toBe('classic');
    expect(document.documentElement.dataset['theme']).toBe('classic');
  });

  it('applies and remembers the selected theme', () => {
    const service = TestBed.inject(ThemeService);

    service.select('neon');

    expect(service.theme()).toBe('neon');
    expect(document.documentElement.dataset['theme']).toBe('neon');
    expect(localStorage.getItem('acervo.theme')).toBe('neon');
  });
});
