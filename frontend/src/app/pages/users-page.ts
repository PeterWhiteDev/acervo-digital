import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminApi } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { AdminCollector, AdminCollectorCollectionsSummary, COLLECTOR_ROLE_LABELS, CollectorRole } from '../core/models';
import { LucideDynamicIcon } from '@lucide/angular';
import { uiIcons } from '../core/ui-icons';

type EditorMode = 'create' | 'edit' | 'password' | null;

@Component({
  selector: 'app-users-page',
  imports: [CommonModule, FormsModule, LucideDynamicIcon],
  styleUrl: './users-page.scss',
  templateUrl: './users-page.html',
})
export class UsersPage implements OnInit {
  private readonly api = inject(AdminApi);
  private readonly auth = inject(AuthService);
  protected readonly icons = uiIcons;
  protected readonly users = signal<AdminCollector[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal('');
  protected readonly query = signal('');
  protected readonly expandedId = signal<string | null>(null);
  protected readonly detailLoadingId = signal<string | null>(null);
  protected readonly summaries = signal<Record<string, AdminCollectorCollectionsSummary>>({});
  protected readonly editorMode = signal<EditorMode>(null);
  protected readonly editingUser = signal<AdminCollector | null>(null);
  protected readonly isAdmin = computed(() => this.auth.isAdmin());
  protected readonly collectorId = computed(() => this.auth.collectorId());
  protected readonly filteredUsers = computed(() => {
    const query = this.query().trim().toLocaleLowerCase();
    if (!query) return this.users();
    return this.users().filter((user) =>
      `${user.displayName} ${user.email} ${user.role}`.toLocaleLowerCase().includes(query),
    );
  });

  protected email = '';
  protected displayName = '';
  protected password = '';
  protected role: 'USER' | 'ADMIN' = 'USER';
  protected active = true;

  ngOnInit(): void {
    this.loadUsers();
  }

  protected loadUsers(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.listUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.showError(error);
      },
    });
  }

  protected toggleDetails(user: AdminCollector): void {
    if (this.expandedId() === user.collectorId) {
      this.expandedId.set(null);
      return;
    }
    this.expandedId.set(user.collectorId);
    if (this.summaries()[user.collectorId]) return;

    this.detailLoadingId.set(user.collectorId);
    this.api.getUserCollections(user.collectorId).subscribe({
      next: (summary) => {
        this.summaries.update((summaries) => ({ ...summaries, [user.collectorId]: summary }));
        this.detailLoadingId.set(null);
      },
      error: (error) => {
        this.detailLoadingId.set(null);
        this.showError(error);
      },
    });
  }

  protected openCreate(): void {
    this.resetForm();
    this.editorMode.set('create');
  }

  protected openEdit(user: AdminCollector): void {
    if (this.isSelf(user)) return;
    this.resetForm();
    this.editingUser.set(user);
    this.email = user.email;
    this.displayName = user.displayName;
    this.role = user.role;
    this.active = user.active;
    this.editorMode.set('edit');
  }

  protected openPasswordReset(user: AdminCollector): void {
    if (this.isSelf(user)) return;
    this.resetForm();
    this.editingUser.set(user);
    this.editorMode.set('password');
  }

  protected closeEditor(): void {
    this.editorMode.set(null);
    this.editingUser.set(null);
  }

  protected saveUser(): void {
    this.error.set('');
    this.saving.set(true);
    const request = {
      email: this.email.trim(),
      displayName: this.displayName.trim(),
      role: this.role,
      active: this.active,
    };
    const user = this.editingUser();
    const operation = user
      ? this.api.updateUser(user.collectorId, request)
      : this.api.createUser({
          email: request.email,
          displayName: request.displayName,
          password: this.password,
        });

    operation.subscribe({
      next: () => {
        this.saving.set(false);
        this.closeEditor();
        this.success.set(user ? 'Cuenta actualizada.' : 'Cuenta creada con rol USER.');
        this.loadUsers();
      },
      error: (error) => {
        this.saving.set(false);
        this.showError(error);
      },
    });
  }

  protected resetUserPassword(): void {
    const user = this.editingUser();
    if (!user) return;
    this.saving.set(true);
    this.error.set('');
    this.api.resetPassword(user.collectorId, this.password).subscribe({
      next: () => {
        this.saving.set(false);
        this.closeEditor();
        this.success.set(`Contraseña restablecida para ${user.email}.`);
      },
      error: (error) => {
        this.saving.set(false);
        this.showError(error);
      },
    });
  }

  protected deactivate(user: AdminCollector): void {
    if (this.isSelf(user)) return;
    if (!window.confirm(`¿Desactivar la cuenta de ${user.email}? Sus colecciones y datos se conservarán.`)) return;
    this.api.deactivateUser(user.collectorId).subscribe({
      next: () => {
        this.success.set('Cuenta desactivada; sus datos se conservan.');
        this.loadUsers();
      },
      error: (error) => this.showError(error),
    });
  }

  protected toggleActive(user: AdminCollector): void {
    if (this.isSelf(user)) return;
    this.api.updateUser(user.collectorId, {
      email: user.email,
      displayName: user.displayName,
      role: user.role,
      active: true,
    }).subscribe({
      next: () => {
        this.success.set('Cuenta reactivada.');
        this.loadUsers();
      },
      error: (error) => this.showError(error),
    });
  }

  protected isSelf(user: AdminCollector): boolean {
    return user.collectorId === this.collectorId();
  }

  protected roleLabel(role: CollectorRole): string {
    return COLLECTOR_ROLE_LABELS[role];
  }

  private resetForm(): void {
    this.error.set('');
    this.editingUser.set(null);
    this.email = '';
    this.displayName = '';
    this.password = '';
    this.role = 'USER';
    this.active = true;
  }

  private showError(error: unknown): void {
    const status = error instanceof HttpErrorResponse ? error.status : 0;
    switch (status) {
      case 401:
        this.error.set('La sesión caducó; vuelve a iniciar sesión.');
        break;
      case 403:
        this.error.set('No tienes permiso para realizar esta operación.');
        break;
      case 404:
        this.error.set('La cuenta o sus datos ya no existen.');
        break;
      case 409:
        this.error.set('El email ya existe o la operación dejaría el sistema sin administrador activo.');
        break;
      default:
        this.error.set('No se pudo completar la operación. Comprueba que los servicios estén disponibles.');
    }
  }
}
