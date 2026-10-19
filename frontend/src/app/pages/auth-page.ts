import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { AuthService } from '../core/auth.service';
import { uiIcons } from '../core/ui-icons';

@Component({
  selector: 'app-auth-page',
  imports: [FormsModule, RouterLink, LucideDynamicIcon],
  styleUrl: './auth-page.scss',
  templateUrl: './auth-page.html',
})
export class AuthPage {
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected readonly icons = uiIcons;
  protected readonly mode = signal<'login' | 'register'>('login');
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected email = '';
  protected password = '';
  protected displayName = '';

  constructor() {
    this.mode.set(this.route.snapshot.data['mode'] === 'register' ? 'register' : 'login');
    if (this.auth.isAuthenticated()) {
      void this.router.navigate(['/catalog']);
    }
  }

  protected submit(): void {
    this.loading.set(true);
    this.error.set('');
    const credentials = { email: this.email.trim(), password: this.password };
    const signIn = () => this.auth.login(credentials).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/catalog';
        void this.router.navigateByUrl(returnUrl);
      },
      error: (error: { status?: number }) => this.fail(error),
    });

    if (this.mode() === 'register') {
      this.auth.register({ ...credentials, displayName: this.displayName.trim() }).subscribe({
        next: () => signIn(),
        error: (error: { status?: number }) => this.fail(error),
      });
      return;
    }
    signIn();
  }

  private fail(error: { status?: number }): void {
    this.loading.set(false);
    this.error.set(error.status === 409
      ? 'Ya existe una cuenta con ese correo.'
      : error.status === 401
        ? 'Correo o contraseña incorrectos.'
        : 'No se pudo conectar con el servicio. Comprueba que los servicios estén iniciados.');
  }
}