import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { AuthService } from './core/auth.service';
import { uiIcons } from './core/ui-icons';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LucideDynamicIcon],
  selector: 'app-root',
  styleUrl: './app-shell.scss',
  templateUrl: './app-shell.html',
})
export class App {
  protected readonly auth = inject(AuthService);
  protected readonly router = inject(Router);
  protected readonly icons = uiIcons;

  protected logout(): void {
    this.auth.logout();
    void this.router.navigate(['/login']);
  }
}
