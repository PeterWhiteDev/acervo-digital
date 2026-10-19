import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { AuthService } from '../core/auth.service';
import { uiIcons } from '../core/ui-icons';
import { Brand } from '../theme/brand';
import { ThemeService } from '../theme/theme.service';

const THEME_COPY = {
  classic: {
    eyebrow: 'TU ARCHIVO, A TU MANERA',
    lines: ['Lo que guardas', 'también cuenta'],
    accent: 'una historia.',
    note: 'Un lugar para ordenar tus libros, películas y todo lo que merece quedarse.',
    index: 'COLECCIONA CON SENTIDO',
  },
  expedition: {
    eyebrow: 'BITÁCORA · LAT 20º 000’ · HALLAZGOS',
    lines: ['Cada objeto', 'guarda un mapa'],
    accent: 'hacia su historia.',
    note: 'Registra reliquias, cartografía tus colecciones y marca con una X lo que aún buscas.',
    index: 'LA AVENTURA EMPIEZA EN TUS ESTANTES',
  },
  neon: {
    eyebrow: 'SYS://ACERVO · NODO 06 · EN LÍNEA',
    lines: ['Memoria', 'persistente para'],
    accent: 'lo que importa.',
    note: 'Archivo personal cifrado. Cada objeto, catalogado; cada recuerdo, a salvo de la lluvia.',
    index: 'IDENTIFÍCATE PARA ACCEDER AL NÚCLEO',
  },
} as const;

@Component({
  selector: 'app-auth-page',
  imports: [FormsModule, RouterLink, LucideDynamicIcon, Brand],
  styleUrl: './auth-page.scss',
  templateUrl: './auth-page.html',
})
export class AuthPage implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly theme = inject(ThemeService);
  protected readonly copy = computed(() => THEME_COPY[this.theme.theme()]);
  protected readonly icons = uiIcons;
  protected readonly mode = signal<'login' | 'register'>('login');
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected email = '';
  protected password = '';
  protected displayName = '';

  ngOnInit(): void {
    this.mode.set(this.route.snapshot.data['mode'] === 'register' ? 'register' : 'login');
    if (this.auth.isAuthenticated()) {
      void this.router.navigate(['/collections']);
    }
  }

  protected submit(): void {
    this.loading.set(true);
    this.error.set('');
    const credentials = { email: this.email.trim(), password: this.password };
    const signIn = () => this.auth.login(credentials).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/collections';
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
    switch (error.status) {
      case 409:
        this.error.set('Ya existe una cuenta con ese correo.');
        break;
      case 401:
        this.error.set('Correo o contraseña incorrectos.');
        break;
      default:
        this.error.set('No se pudo conectar con el servicio. Comprueba que los servicios estén iniciados.');
    }
  }
}