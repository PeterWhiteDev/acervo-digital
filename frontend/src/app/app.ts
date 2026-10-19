import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { AuthService } from './core/auth.service';
import { uiIcons } from './core/ui-icons';
import { Brand } from './theme/brand';
import { ThemeBackdrop } from './theme/theme-backdrop';
import { ThemeSwitcher } from './theme/theme-switcher';
import { ThemeService } from './theme/theme.service';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LucideDynamicIcon, Brand, ThemeBackdrop, ThemeSwitcher],
  selector: 'app-root',
  styleUrl: './app-shell.scss',
  templateUrl: './app-shell.html',
})
export class App {
  protected readonly auth = inject(AuthService);
  protected readonly router = inject(Router);
  protected readonly theme = inject(ThemeService);
  protected readonly icons = uiIcons;
  protected readonly dayOfYear = Math.ceil(
    (Date.now() - new Date(new Date().getFullYear(), 0, 0).getTime()) / 86_400_000,
  );

  protected logout(): void {
    this.auth.logout();
    void this.router.navigate(['/login']);
  }
}
