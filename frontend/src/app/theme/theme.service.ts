import { DOCUMENT } from '@angular/common';
import { inject, Injectable, signal } from '@angular/core';

export type UiTheme = 'classic' | 'expedition' | 'neon';

const STORAGE_KEY = 'acervo.theme';
const THEMES: readonly UiTheme[] = ['classic', 'expedition', 'neon'];
const BROWSER_COLORS: Record<UiTheme, string> = {
  classic: '#172d2a',
  expedition: '#3a2617',
  neon: '#04070e',
};

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly state = signal<UiTheme>(readStoredTheme());

  readonly theme = this.state.asReadonly();

  constructor() {
    this.apply(this.state());
  }

  select(theme: UiTheme): void {
    this.state.set(theme);
    localStorage.setItem(STORAGE_KEY, theme);
    this.apply(theme);
  }

  private apply(theme: UiTheme): void {
    this.document.documentElement.dataset['theme'] = theme;
    this.document.querySelector('meta[name="theme-color"]')?.setAttribute('content', BROWSER_COLORS[theme]);
  }
}

function readStoredTheme(): UiTheme {
  const stored = localStorage.getItem(STORAGE_KEY);
  return THEMES.find((theme) => theme === stored) ?? 'classic';
}
