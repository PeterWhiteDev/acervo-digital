import { Component, inject } from '@angular/core';
import { ThemeService } from './theme.service';

@Component({
  selector: 'app-theme-backdrop',
  styleUrl: './theme-backdrop.scss',
  template: `
    @switch (theme.theme()) {
      @case ('expedition') {
        <div class="scene expedition" aria-hidden="true">
          <svg class="contours" viewBox="0 0 800 600" preserveAspectRatio="xMidYMid slice">
            <g fill="none" stroke="#7a5a32" stroke-width="1.1">
              <path d="M90 120 C180 60 300 90 330 170 C360 250 260 300 170 280 C80 260 30 180 90 120 Z" />
              <path d="M120 140 C190 100 280 120 300 180 C318 235 245 265 180 255 C115 245 80 190 120 140 Z" />
              <path d="M150 160 C200 135 255 150 268 188 C278 222 230 238 190 230 C150 222 125 190 150 160 Z" />
              <path d="M520 380 C610 330 740 360 760 450 C780 540 650 580 570 550 C490 520 450 420 520 380 Z" />
              <path d="M550 405 C620 370 715 395 728 458 C740 520 645 545 585 524 C525 503 500 433 550 405 Z" />
            </g>
          </svg>
          <svg class="route" viewBox="0 0 800 600" preserveAspectRatio="none">
            <path d="M40 560 C170 470 210 520 300 430 S470 300 560 330 S700 220 742 120" fill="none" stroke="#9c3b1f" stroke-dasharray="2 12" stroke-linecap="round" stroke-width="3" />
            <path d="M728 106 L756 134 M756 106 L728 134" stroke="#9c3b1f" stroke-linecap="round" stroke-width="5" />
          </svg>
          <svg class="compass" viewBox="0 0 200 200">
            <g fill="none" stroke="#5a3a22">
              <circle cx="100" cy="100" r="96" stroke-width="1.5" />
              <circle cx="100" cy="100" r="88" stroke-dasharray="1 5" stroke-width="3" />
              <circle cx="100" cy="100" r="58" />
            </g>
            <g fill="#5a3a22">
              <path d="M100 6 L112 100 L100 194 L88 100 Z" />
              <path d="M6 100 L100 88 L194 100 L100 112 Z" />
            </g>
            <g fill="#9c7a44">
              <path d="M100 30 L106 100 L100 100 Z" transform="rotate(45 100 100)" />
              <path d="M100 30 L106 100 L100 100 Z" transform="rotate(135 100 100)" />
              <path d="M100 30 L106 100 L100 100 Z" transform="rotate(225 100 100)" />
              <path d="M100 30 L106 100 L100 100 Z" transform="rotate(315 100 100)" />
            </g>
            <text x="100" y="26" fill="#5a3a22" font-family="Cinzel, serif" font-size="13" text-anchor="middle">N</text>
          </svg>
          <svg class="submarine" viewBox="0 0 240 90">
            <path d="M14 52 C30 28 82 22 140 24 C188 26 222 38 228 52 C222 66 188 76 140 78 C82 80 30 74 14 52 Z" fill="#3b2a1a" />
            <path d="M92 26 L102 6 H138 L146 26 Z" fill="#3b2a1a" />
            <path d="M228 52 L238 38 V66 Z" fill="#3b2a1a" />
            <g fill="#e9d8ac"><circle cx="70" cy="50" r="5" /><circle cx="96" cy="50" r="5" /><circle cx="122" cy="50" r="5" /><circle cx="148" cy="50" r="5" /><circle cx="174" cy="50" r="5" /></g>
          </svg>
        </div>
      }
      @case ('neon') {
        <div class="scene neon" aria-hidden="true">
          <div class="glow glow-a"></div>
          <div class="glow glow-b"></div>
          <svg class="skyline" viewBox="0 0 1200 260" preserveAspectRatio="none">
            <path d="M0 260 V170 H40 V120 H70 V150 H110 V80 H130 V60 H150 V80 H170 V140 H210 V100 H250 V160 H290 V40 H300 V20 H310 V40 H330 V130 H380 V90 H420 V150 H470 V110 H500 V70 H540 V140 H590 V100 H610 V55 H650 V100 H690 V150 H730 V85 H780 V125 H820 V30 H835 V10 H845 V30 H860 V140 H910 V95 H960 V155 H1000 V75 H1040 V120 H1090 V60 H1120 V145 H1160 V110 H1200 V260 Z" fill="#070c18" />
            <g fill="#ff8a2a" opacity=".85"><rect x="118" y="92" width="4" height="3" /><rect x="296" y="60" width="4" height="3" /><rect x="296" y="78" width="4" height="3" /><rect x="626" y="70" width="5" height="3" /><rect x="828" y="48" width="4" height="3" /><rect x="1010" y="90" width="5" height="3" /></g>
            <g fill="#3fd6ff" opacity=".8"><rect x="520" y="86" width="4" height="3" /><rect x="750" y="100" width="4" height="3" /><rect x="1100" y="78" width="4" height="3" /><rect x="190" y="110" width="4" height="3" /><rect x="940" y="112" width="4" height="3" /></g>
            <path d="M0 259 H1200" stroke="#3fd6ff" stroke-opacity=".5" />
          </svg>
          <div class="floor"></div>
          <div class="rain"></div>
          <div class="scanlines"></div>
          <div class="sweep"></div>
        </div>
      }
    }
  `,
})
export class ThemeBackdrop {
  protected readonly theme = inject(ThemeService);
}
