import { Component, inject } from '@angular/core';
import { ThemeService, UiTheme } from './theme.service';

@Component({
  selector: 'app-theme-switcher',
  styleUrl: './theme-switcher.scss',
  template: `
    <div class="switcher" role="group" aria-label="Estilo de la interfaz">
      <span class="switcher-label" aria-hidden="true">Estilo</span>
      @for (option of options; track option.id) {
        <button
          type="button"
          [class.active]="theme.theme() === option.id"
          [attr.aria-pressed]="theme.theme() === option.id"
          [title]="option.description"
          (click)="theme.select(option.id)"
        >
          <span class="swatch" [class]="'swatch swatch-' + option.id" aria-hidden="true"></span>
          <span class="option-label">{{ option.label }}</span>
        </button>
      }
    </div>
  `,
})
export class ThemeSwitcher {
  protected readonly theme = inject(ThemeService);
  protected readonly options: ReadonlyArray<{ id: UiTheme; label: string; description: string }> = [
    { id: 'classic', label: 'Clásico', description: 'Interfaz original, clara y sobria' },
    { id: 'expedition', label: 'Expedición', description: 'Mapas, bitácora y aventura submarina' },
    { id: 'neon', label: 'Neón', description: 'Ciencia ficción, HUD y ciudad nocturna' },
  ];
}
