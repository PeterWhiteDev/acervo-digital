import { Component, inject } from '@angular/core';
import { LucideDynamicIcon } from '@lucide/angular';
import { uiIcons } from '../core/ui-icons';
import { ThemeService } from './theme.service';

@Component({
  selector: 'app-brand',
  imports: [LucideDynamicIcon],
  styleUrl: './brand.scss',
  template: `
    @switch (theme.theme()) {
      @case ('expedition') {
        <svg class="mark mark-expedition" viewBox="0 0 64 64" aria-hidden="true">
          <defs>
            <linearGradient id="acv-brand-brass" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0" stop-color="#f3d78f" />
              <stop offset=".5" stop-color="#b8862f" />
              <stop offset="1" stop-color="#6e4a17" />
            </linearGradient>
            <radialGradient id="acv-brand-sea" cx="45%" cy="38%" r="65%">
              <stop offset="0" stop-color="#4c9aa1" />
              <stop offset="1" stop-color="#0b2a31" />
            </radialGradient>
          </defs>
          <circle cx="32" cy="32" r="30" fill="url(#acv-brand-brass)" />
          <circle cx="32" cy="32" r="21" fill="url(#acv-brand-sea)" stroke="#5c3d12" stroke-width="2" />
          <g fill="#5c3d12">
            <circle cx="57.5" cy="32" r="1.8" /><circle cx="50" cy="50" r="1.8" /><circle cx="32" cy="57.5" r="1.8" /><circle cx="14" cy="50" r="1.8" />
            <circle cx="6.5" cy="32" r="1.8" /><circle cx="14" cy="14" r="1.8" /><circle cx="32" cy="6.5" r="1.8" /><circle cx="50" cy="14" r="1.8" />
          </g>
          <path d="M32 14 L36 32 L32 50 L28 32 Z" fill="#f3e2b5" />
          <path d="M32 14 L36 32 L28 32 Z" fill="#b5432a" />
          <circle cx="32" cy="32" r="2.4" fill="#2a1a0a" />
          <path d="M18 23 A16 16 0 0 1 29 15" fill="none" stroke="rgba(255,255,255,.5)" stroke-linecap="round" stroke-width="2" />
        </svg>
        <span class="words"><span class="name">Acervo</span><span class="tag">Cuaderno de expedición</span></span>
      }
      @case ('neon') {
        <svg class="mark mark-neon" viewBox="0 0 64 64" aria-hidden="true">
          <polygon points="32,3 57,17.5 57,46.5 32,61 7,46.5 7,17.5" fill="rgba(63,214,255,.08)" stroke="#3fd6ff" stroke-width="2" />
          <polygon points="32,12 49,22 49,42 32,52 15,42 15,22" fill="none" stroke="rgba(63,214,255,.4)" stroke-dasharray="3 3" />
          <path d="M21 42 L32 21 L43 42" fill="none" stroke="#ff8a2a" stroke-linejoin="miter" stroke-width="3.5" />
          <path d="M25.5 35 H38.5" stroke="#ff8a2a" stroke-width="2.5" />
          <circle cx="32" cy="3" r="2" fill="#ff8a2a" />
        </svg>
        <span class="words"><span class="name">ACERVO</span><span class="tag">// archivo orbital · v0.6</span></span>
      }
      @default {
        <span class="mark mark-classic"><svg [lucideIcon]="icons.LucideBookOpen" aria-hidden="true"></svg></span>
        <span class="words"><span class="name">acervo<span class="dot">.</span></span></span>
      }
    }
  `,
})
export class Brand {
  protected readonly theme = inject(ThemeService);
  protected readonly icons = uiIcons;
}
